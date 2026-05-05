package com.mrdben.autodishes.familygraphs.data

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.*

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyRepository (
    private val familyDao : FamilyDao,
    private val elderDao : ElderDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val currentUserId: StateFlow<Int> = TokenManager.userIdFlow
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFamily: StateFlow<Family> = TokenManager.userIdFlow
        .filter { it != 0 }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            initFamilyData(userId)
            familyDao.getFamilyFlow(userId)
        }
        .map { it ?: DEFAULT_FAMILY }
        .stateIn(  // 关键：转成热流，只执行一次
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_FAMILY
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val elders: StateFlow<ElderList> = TokenManager.userIdFlow
        .filter { it != 0 }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            updateEldersFromServer()
            syncElderWithServer()
            elderDao.getAllEldersFlow(userId)
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    suspend fun initFamilyData(userId: Int) {
        Log.d("FamilyRepo","initialize family repo data with userId: $userId")
        if (familyDao.getFamilyFirst(userId) == null) { // no family data locally
            Log.d("FamilyRepo","no data from familyDao, check with server")
            val serverFamily = getFamilyFromServer()  // try to get from server
            if (serverFamily != null) {
                Log.d("FamilyRepo","got data from server")
                familyDao.insertFamily(serverFamily)  // use server data save to local
            } else {    // no data on server
                Log.d("FamilyRepo","no data from server, create default")
                familyDao.insertFamily(
                    DEFAULT_FAMILY.copy(userId = userId)
                )
                syncFamilyWithServer()
            }
        } else {  // have data locally
            Log.d("FamilyRepo","data from Local, sync to server.")
            syncFamilyWithServer()
        }
    }

    fun updateFamily(updatedFamily: Family) {
        ioScope.launch {
            val familyToSync = updatedFamily.copy(
                syncStatus = SyncStatus.TO_UPLOAD,
            )
            familyDao.updateFamily(familyToSync)
            syncFamilyWithServer()
        }
    }

    fun addElder(elder: Elder) {
        ioScope.launch {
            Log.d("FamilyRepo","addElder called: ${elder.elderId}")
            val existingElder = getElderById(elder.elderId)
            if (existingElder == null) {
                Log.d("FamilyRepo","addElder: No existing. ${elder.elderId}")
                val elderToSync = elder.copy(
                    userId = currentUserId.value,
                    elderId = "member_${System.currentTimeMillis()}",
                    syncStatus = SyncStatus.TO_UPLOAD,
                )
                elderDao.insertElder(elderToSync)
                syncElderWithServer()
            }
        }
    }

    // 删：删除老人
    fun removeElder(elderId: String) {
        ioScope.launch {
            val existingElder = getElderById(elderId)
            if (existingElder != null) {
                val elderToSync = existingElder.copy(
                    syncStatus = SyncStatus.TO_DELETE,
                    isDeleted = true
                )
                elderDao.updateElder(elderToSync)
                syncElderWithServer()
            }
        }
    }

    // 改：修改老人信息
    fun updateElder(updatedElder: Elder) {
        ioScope.launch {
            val elderToSync = updatedElder.copy(
                syncStatus = SyncStatus.TO_UPLOAD,
            )
            elderDao.updateElder(elderToSync)
            syncElderWithServer()
        }
    }

    // 查：获取单个老人
    fun getElderById(elderId: String): Elder? {
        return elders.value.find { it.elderId == elderId }
    }

    private suspend fun syncFamilyWithServer() {
        val userId = currentUserId.value
        Log.d("FamilyRepo","准备同步family数据, for user: $userId")
        val localFamily = familyDao.getNeedSyncFamilies(userId = userId).firstOrNull()
        if (localFamily == null) {
            Log.d("FamilyRepo","没有需要同步的family数据, for user: $userId")
            return
        }
        Log.d("FamilyRepo","开始同步family数据")
        val (code, message, newFamilyId) = pushFamilyWithServer(localFamily)
        when (code) {
            200 -> {
                familyDao.markFamiliesAsSynced(userId = userId, familyId = newFamilyId, updatedAt = message)
                familyDao.cleanDeletedSyncedFamilies(userId = userId)
                Log.d("FamilyRepo", "Family 本地数据清理完成。")
            }
            409 -> {
                val family = getFamilyFromServer()
                if (family != null) {
                    familyDao.deleteAllFamilies()
                    familyDao.insertFamily(family)
                    Log.d("FamilyRepo", "本地数据库已更新为云端最新数据")
                }
            }
        }
    }
    // sync up to backend server
    private suspend fun pushFamilyWithServer(localFamily: Family): Triple<Int, String, Int> {
        Log.d("FamilyRepo", "Family 数据上传开始。")
        val request = FamilyDto( localFamily )
        val response = safeApiCall{ RetrofitClient.familyApiService.pushFamilyToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val updatedTime = response.data.updatedAt
                val familyId = response.data.itemId
                Log.d("FamilyRepo", "Family: $familyId 数据上传成功。更新时间： $updatedTime")
                return Triple(200, updatedTime, familyId)
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Family 数据上传失败。 错误码: $code, 错误信息: $message")
                return Triple(code, message, 0)
            }
        }
   }
    private suspend fun getFamilyFromServer() : Family? {
        Log.d("FamilyRepo", "Family 数据下载开始。")
        val response = safeApiCall{ RetrofitClient.familyApiService.getFamilyFromServer() }
        when (response) {
            is ResultWrapper.Success -> {
                val familyDto = response.data
                Log.d("FamilyRepo", "Family 数据下载成功。")
                return Family(familyDto)
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Family 下载失败。 错误码: $code, 错误信息: $message")
                return null
            }
        }
    }
    private suspend fun syncElderWithServer() {
        val userId = currentUserId.value
        Log.d("FamilyRepo","准备同步elder数据, for user: $userId")
        val localElders = elderDao.getNeedSyncElders(userId = userId)
        if (localElders.isEmpty()) {
            Log.d("FamilyRepo","没有需要同步的elder数据, for user: $userId")
            return
        }
        Log.d("FamilyRepo","开始同步elder数据")
        for (elder in localElders) {
            val (code, message, serverId) = pushElderWithServer(elder)
            when (code) {
                200 -> {
                    val elderToSave = elder.copy(
                        syncStatus = SyncStatus.SYNCED,
                        serverId = serverId,
                        updatedAt = message
                    )
                    elderDao.updateElder(elderToSave)
                    Log.d("FamilyRepo", "Elder: ${elder.elderId} 上传成功。")
                }
                409 -> {
                    Log.d("FamilyRepo", "Elder: ${elder.elderId} 数据已过期。从服务器下载")
                    val newElder = pullElderFromServer(elder.serverId)
                    if (newElder != null) {
                        elderDao.updateElder(newElder)
                        Log.d("FamilyRepo", "本地Elder: ${elder.elderId} 数据已更新为云端最新数据")
                    }
                }
            }
        }
        elderDao.cleanDeletedSyncedElders(userId)
        Log.d("FamilyRepo", "Elders 本地数据清理完成。")
    }

    private suspend fun pushElderWithServer(localElder: Elder): Triple<Int, String, Int> {
        Log.d("FamilyRepo", "Elder: ${localElder.elderId} 数据上传开始。")
        val request = ElderDto( localElder )
        val response = safeApiCall{ RetrofitClient.elderApiService.pushElderToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val updatedTime = response.data.updatedAt
                val serverId = response.data.itemId
                Log.d("FamilyRepo", "Elder: ${localElder.elderId} 数据上传成功。更新时间： $updatedTime")
                return Triple(200, updatedTime, serverId)
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Elder: ${localElder.elderId} 数据上传失败。 错误码: $code, 错误信息: $message")
                return Triple(code, message, 0)
            }
        }
    }
    private suspend fun pullElderFromServer(serverId : Int) : Elder? {
        Log.d("FamilyRepo", "Elder 数据下载开始, serverId = ${serverId}。")
        val request = PullElderRequest(serverId = serverId)
        val response = safeApiCall{ RetrofitClient.elderApiService.pullElderFromServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val elderDto = response.data
                Log.d("FamilyRepo", "Elder 数据下载成功, serverId = ${serverId}。")
                return Elder(elderDto)
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Elder 下载失败。 错误码: $code, 错误信息: $message")
                return null
            }
        }
    }
    private suspend fun updateEldersFromServer() {
        Log.d("FamilyRepo", "Elders 全量数据下载开始。")
        val response = safeApiCall{ RetrofitClient.elderApiService.getEldersFromServer() }
        when (response) {
            is ResultWrapper.Success -> {
                val elderListDto = response.data
                Log.d("FamilyRepo", "Elders 数据下载成功。 ${elderListDto.elderList.size} 条记录已下载。")
                for (elderDto in elderListDto.elderList) {
                    val newElder = Elder(elderDto)
                    updateElderDao(newElder)
                }
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Elders 下载失败。 错误码: $code, 错误信息: $message")
            }
        }
    }

    private suspend fun updateElderDao(newElder: Elder) {
        val existingElder = getElderById(newElder.elderId)
        if (existingElder == null){
            if (!newElder.isDeleted) {
                elderDao.insertElder(newElder)
            }
        } else {
            if (existingElder.syncStatus == SyncStatus.SYNCED){ //本地未修改
                elderDao.updateElder(newElder)  // 跟新为服务器数据
            }
            // else: 本地已经修改的数据， 通过 syncElderWithServer 方式刷新
        }
    }
}
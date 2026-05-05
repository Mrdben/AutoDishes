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
            Log.d("FamilyRepo", "flatMapLatest 被触发，userId: $userId")
            initFamilyData(userId)
            familyDao.getFamilyFlow(userId)
        }
        .map { it ?: DEFAULT_FAMILY }
        .stateIn(  // 关键：转成热流，只执行一次
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_FAMILY
        )

    val elders: StateFlow<ElderList> = elderDao.getAllEldersFlow()
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
            val existingElder = getElderById(elder.elderId)
            if (existingElder == null) {
                elderDao.insertElder(elder)
            }
        }
    }

    fun addElder2Family(elder: Elder) {
        ioScope.launch {
            val userId = currentUserId.value
            val family = familyDao.getFamilyFirst(userId) ?: return@launch
            if (!family.elderIds.contains(elder.elderId)) {
                val newIds = family.elderIds + elder.elderId
                val updatedFamily = family.copy(
                    elderIds = newIds,
                    syncStatus = SyncStatus.TO_UPLOAD,
                )
                familyDao.updateFamily(updatedFamily)
                syncFamilyWithServer()
            }
        }
    }

    // 删：删除老人
    fun removeElder(elderId: String) {
        ioScope.launch {
            elderDao.deleteElderById(elderId)
        }
    }

    fun removeElderFromFamily(elderId: String) {
        ioScope.launch {
            val userId = currentUserId.value
            val family = familyDao.getFamilyFirst(userId) ?: return@launch
            val newIds = family.elderIds.filter { it != elderId }
            val updatedFamily = family.copy(
                elderIds = newIds,
                syncStatus = SyncStatus.TO_UPLOAD,
            )
            familyDao.updateFamily(updatedFamily)
            syncFamilyWithServer()
        }
    }

    // 改：修改老人信息
    fun updateElder(updatedElder: Elder) {
        ioScope.launch {
            elderDao.updateElder(updatedElder)
        }
    }

    // 查：获取单个老人
    fun getElderById(elderId: String): Elder? {
        return elders.value.find { it.elderId == elderId }
    }

    suspend fun syncFamilyWithServer() {
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
                val familyId = response.data.familyId
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
}
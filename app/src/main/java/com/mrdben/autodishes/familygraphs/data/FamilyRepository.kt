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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    private val userIdFlow = MutableStateFlow(TokenManager.loginUserId)
//    fun getCurrentFamilyFlow(): Flow<Family> {
//        Log.d("FamilyRepo","getCurrentFamilyFlow: userid = ${TokenManager.loginUserId}")
//        return familyDao.getFamilyFlow(TokenManager.loginUserId)
//            .map { it ?: DEFAULT_FAMILY }
//    }
@OptIn(ExperimentalCoroutinesApi::class)
val currentFamily: Flow<Family> = userIdFlow
        .filter { it != 0 }
        .flatMapLatest { userId ->
            familyDao.getFamilyFlow(userId)
        }
        .map { it ?: DEFAULT_FAMILY }
    val elders: StateFlow<ElderList> = elderDao.getAllEldersFlow()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    suspend fun init() {
        Log.d("FamilyRepo","init function external call.")
        val userId = TokenManager.loginUserId
        userIdFlow.value = userId
        if (userId == 0) {
            Log.d("FamilyRepo","userid is ZERO")
            return
        }
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
            val userId = TokenManager.loginUserId
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
            val userId = TokenManager.loginUserId
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
        val userId = TokenManager.loginUserId
        Log.d("FamilyRepo","准备同步family数据, for user: $userId")
        val localFamily = familyDao.getNeedSyncFamilies(userId = userId).firstOrNull() ?: return
        Log.d("FamilyRepo","开始同步family数据")
        val (code, message) = pushFamilyWithServer(localFamily)
        when (code) {
            200 -> {
                familyDao.markFamiliesAsSynced(familyId = localFamily.familyId, updatedAt = message)
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
    private suspend fun pushFamilyWithServer(localFamily: Family): Pair<Int, String> {
        Log.d("FamilyRepo", "Family 数据上传开始。")
        val request = FamilyDto( localFamily )
        val response = safeApiCall{ RetrofitClient.familyApiService.pushFamilyToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val updatedTime = response.data.updatedAt
                Log.d("FamilyRepo", "Family 数据上传成功。更新时间： $updatedTime")
                return Pair(200, updatedTime)
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "Family 数据上传失败。 错误码: $code, 错误信息: $message")
                return Pair(code, message)
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
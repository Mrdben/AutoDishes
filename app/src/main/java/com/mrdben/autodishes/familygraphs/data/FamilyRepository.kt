package com.mrdben.autodishes.familygraphs.data

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.*

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyRepository (
    private val familyDao : FamilyDao,
    private val elderDao : ElderDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    val currentFamily: Flow<Family> = familyDao.getFamilyFlow()
        .map { it ?: DEFAULT_FAMILY }
    val elders: StateFlow<ElderList> = elderDao.getAllEldersFlow()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    init {
        ioScope.launch {
            if (familyDao.getFamilyFirst() == null) {
                familyDao.insertFamily(DEFAULT_FAMILY)
            }
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
            val family = familyDao.getFamilyFirst() ?: return@launch
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
            val family = familyDao.getFamilyFirst() ?: return@launch
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

    private suspend fun syncFamilyWithServer() {
        Log.d("FamilyRepo","开始同步family数据")
        val localFamily = familyDao.getNeedSyncFamilies().firstOrNull() ?: return
        val (code, message) = pushFamilyWithServer(localFamily)
        when (code) {
            200 -> {
                familyDao.markFamiliesAsSynced(familyId = localFamily.familyId, updatedAt = message)
                familyDao.cleanDeletedSyncedFamilies()
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
package com.mrdben.autodishes.familygraphs.data

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.UserProfile
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.*
import com.mrdben.autodishes.utils.getCurrentUtcTime

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyRepository (
    private val elderDao : ElderDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val currentUserProfile: StateFlow<UserProfile> = TokenManager.userProfile

    @OptIn(ExperimentalCoroutinesApi::class)
    // elders数据流仅用来更新UI，不能用于repo中读取数据
    val elders: StateFlow<ElderList> = currentUserProfile
        .filter { it.userId != 0 }
        .distinctUntilChanged()
        .flatMapLatest {
            elderDao.getAllEldersFlow(it.userId)
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    init {
        ioScope.launch {
            currentUserProfile.collect {
                // currentUserProfile 会同步触发 elders的流和 syncElderWithServer
                // 两个线程并发，有竞争冒险
                // currentUserProfile 应该从dao读取数据，而不是从elders获取数据
                if (currentUserProfile.value.userId != 0) {
                    syncElderWithServer()
                }
            }
        }
    }
    fun addElder(elder: Elder) {
        ioScope.launch {
            Log.d("FamilyRepo","addElder called: ${elder.elderId}")
            val existingElder = getElderById(elder.elderId)
            if (existingElder == null) {
                Log.d("FamilyRepo","addElder: No existing. ${elder.elderId}")
                val elderToSync = elder.copy(
                    userId = currentUserProfile.value.userId,
                    elderId = "member_${System.currentTimeMillis()}",
                    syncStatus = SyncStatus.TO_UPLOAD,
                    createdAt = getCurrentUtcTime(),
                    updatedAt = getCurrentUtcTime()
                )
                elderDao.updateElder(elderToSync)
                syncElderWithServer()
            }
        }
    }

    // 删：删除老人
    fun removeElder(elderId: String) {
        ioScope.launch {
            val existingElder = getElderById(elderId)
            if (existingElder != null) {
                elderDao.softDeleteElder(elderId)
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

    suspend fun syncElderWithServer() {
        val userId = currentUserProfile.value.userId
        if (userId == 0) return
        Log.d("FamilyRepo","准备同步家庭成员数据到服务器, for user: $userId")
        // from APP to server
        val localElders = elderDao.getNeedSyncElders(userId = userId)
        if (localElders.isEmpty()) {
            Log.d("FamilyRepo","没有需要同步的本地家庭成员数据, for user: $userId")
        } else {
            Log.d("FamilyRepo", "开始同步家庭成员数据到服务器, 共 ${localElders.size} 条")
            val serverInfo: List<UpdateInfo> = pushEldersToServer(localElders)
            for (info in serverInfo) {
                val elder = elderDao.getElderById(info.elderId)
                if (elder == null) {
                    Log.d("FamilyRepo", "无法找到家庭成员：${info.elderId}")
                    continue
                }
                val elderToSave = elder.copy(
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = info.updatedAt
                )
                elderDao.updateElder(elderToSave)
                Log.d("FamilyRepo", "家庭成员更新成功->: ${elder.name} >>> ${elder.elderId} >>> ${elderToSave.updatedAt}。")
            }
            Log.d("FamilyRepo", "完成同步家庭成员数据到服务器, for user: $userId")
        }
        Log.d("FamilyRepo","准备拉取家庭成员数据到本地, for user: $userId")
        // from server to APP
        val infoList: List<UpdateInfo> = elderDao.getAllEldersInfo(userId)
        Log.d("FamilyRepo","向服务器发送 ${infoList.size} 条成员数据。")
        for (info in infoList) {
            Log.d("FamilyRepo","  - 发送成员数据: ${info.elderId} >>> ${info.updatedAt}")
        }
        val updatedElderList: ElderList = pullEldersFromServer(infoList)
        Log.d("FamilyRepo","服务器返回 ${updatedElderList.size} 条成员数据。")
        for (thisElder in updatedElderList) {
            elderDao.updateElder(thisElder)
            Log.d("FamilyRepo", "家庭成员更新成功<-: ${thisElder.name} >>> ${thisElder.elderId} >>> ${thisElder.updatedAt}。")
        }
        Log.d("FamilyRepo","完成拉取家庭成员数据到本地, for user: $userId")
        elderDao.cleanDeletedElders(userId)
        Log.d("FamilyRepo", "家庭成员 本地数据清理完成。")
    }

    private suspend fun pushEldersToServer(localElders: ElderList): List<UpdateInfo> {
        // prepare request info
        val elderModels = mutableListOf<ElderDto>()
        for (elder in localElders) {
            elderModels.add(ElderDto(elder))
        }
        val request = ElderListDto(elderList = elderModels)
        // server call
        val response = safeApiCall{ RetrofitClient.elderApiService.pushEldersToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                return response.data.infoList
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "家庭成员 批量上传失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
    private suspend fun pullEldersFromServer(pullList : List<UpdateInfo>) : ElderList {
        val userId = currentUserProfile.value.userId
        val request = UpdateInfoList(infoList = pullList)
        val response = safeApiCall{ RetrofitClient.elderApiService.pullEldersFromServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val elderListDto = response.data.elderList
                val elderList = mutableListOf<Elder>()
                for (elderDto in elderListDto) {
                    elderList.add(Elder(userId, elderDto))
                }
                return elderList
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("FamilyRepo", "家庭成员 批量下载失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
}
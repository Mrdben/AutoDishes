package com.mrdben.autodishes.dishgraphs.data

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.UserProfile
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.dishgraphs.api.DishDto
import com.mrdben.autodishes.dishgraphs.api.DishListDto
import com.mrdben.autodishes.dishgraphs.api.UpdateInfo
import com.mrdben.autodishes.dishgraphs.api.UpdateInfoList

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishRepository (
    private val dishDao: DishDao
){
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val currentUserProfile: StateFlow<UserProfile> = TokenManager.userProfile

    // 菜品 Flow（实时更新）
    @OptIn(ExperimentalCoroutinesApi::class)
    val dishes: StateFlow<DishList> = currentUserProfile
        .filter { it.userId != 0 }
        .distinctUntilChanged()
        .flatMapLatest {
            dishDao.getAllDishesFlow(it.userId)
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )
    init {
        ioScope.launch {
            currentUserProfile.collect {
                if (currentUserProfile.value.userId != 0) {
                    syncDishWithServer()
                }
            }
        }
    }
    /** 2. 添加一个 Dish */
    fun addDish(dish: Dish) {
        ioScope.launch {
            Log.d("DishRepo","addDish called: ${dish.dishId}")
            val existingDish = getDishById(dish.dishId)
            if (existingDish == null) {
                Log.d("DishRepo","addDish: No existing. ${dish.dishId}")
                val dishToSync = dish.copy(
                    userId = currentUserProfile.value.userId,
                    dishId = "dish_${System.currentTimeMillis()}",
                    syncStatus = SyncStatus.TO_UPLOAD,
                )
                dishDao.updateDish(dishToSync)
                syncDishWithServer()
            }
        }
    }

    /** 3. 根据 ID 删除一个 Dish */
    fun removeDishById(dishId: String) {
        ioScope.launch {
            val existingDish = getDishById(dishId)
            if (existingDish != null) {
                dishDao.softDeleteDish(dishId)
                syncDishWithServer()
            }
        }
    }

    /** 4. 根据 ID 更新整个 Dish 对象 */
    fun updateDish(updatedDish: Dish) {
        ioScope.launch {
            val dishToSync = updatedDish.copy(
                syncStatus = SyncStatus.TO_UPLOAD,
            )
            dishDao.updateDish(dishToSync)
            syncDishWithServer()
        }
    }

    /** 5. 根据 ID 获取单个 Dish（找不到返回 null） */
    fun getDishById(id: String): Dish? {
        return dishes.value.find { it.dishId == id }
    }

    fun like(dishId: String, delta: Int = 1) {
        ioScope.launch {
            val dish = getDishById(dishId)
            if (dish != null) {
                val newLevel = (dish.favoriteLevel + delta)
                    .coerceAtMost(10)
                    .coerceAtLeast(-10)
                dishDao.updateDishLike(dishId, newLevel)
            }
            //syncDishWithServer() // TODO: 什么时候更新后端服务器？
        }
    }

    suspend fun syncDishWithServer() {
        val userId = currentUserProfile.value.userId
        if (userId == 0) return
        Log.d("DishRepo","准备同步餐品信息数据到服务器, for user: $userId")
        // from APP to server
        val localDishes = dishDao.getNeedSyncDishes(userId = userId)
        if (localDishes.isEmpty()) {
            Log.d("DishRepo","没有需要同步的本地餐品信息数据, for user: $userId")
        } else {
            Log.d("DishRepo", "开始同步餐品信息数据到服务器, 共 ${localDishes.size} 条")
            val serverInfo: List<UpdateInfo> = pushDishesToServer(localDishes)
            for (info in serverInfo) {
                val dish = dishDao.getDishById(info.dishId)
                if (dish == null) {
                    Log.d("DishRepo", "无法找到餐品信息：${info.dishId}")
                    continue
                }
                val elderToSave = dish.copy(
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = info.updatedAt
                )
                dishDao.updateDish(elderToSave)
                Log.d("DishRepo", "餐品信息更新成功->: ${dish.name} >>> ${dish.dishId} >>> ${elderToSave.updatedAt}。")
            }
            Log.d("DishRepo", "完成同步餐品信息数据到服务器, for user: $userId")
        }
        Log.d("DishRepo","准备拉取餐品信息数据到本地, for user: $userId")
        // from server to APP
        val infoList: List<UpdateInfo> = dishDao.getAllDishesInfo(userId)
        Log.d("DishRepo","向服务器发送 ${infoList.size} 条成员数据。")
        for (info in infoList) {
            Log.d("DishRepo","  - 发送成员数据: ${info.dishId} >>> ${info.updatedAt}")
        }
        val updatedDishList: DishList = pullDishesFromServer(infoList)
        Log.d("DishRepo","服务器返回 ${updatedDishList.size} 条成员数据。")
        for (thisDish in updatedDishList) {
            dishDao.updateDish(thisDish)
            Log.d("DishRepo", "餐品信息更新成功<-: ${thisDish.name} >>> ${thisDish.dishId} >>> ${thisDish.updatedAt}。")
        }
        Log.d("DishRepo","完成拉取餐品信息数据到本地, for user: $userId")
        dishDao.cleanDeletedDishes(userId)
        Log.d("DishRepo", "餐品信息 本地数据清理完成。")
    }

    private suspend fun pushDishesToServer(localDishes: DishList): List<UpdateInfo> {
        // prepare request info
        val elderModels = mutableListOf<DishDto>()
        for (dish in localDishes) {
            elderModels.add(DishDto(dish))
        }
        val request = DishListDto(dishList = elderModels)
        // server call
        val response = safeApiCall{ RetrofitClient.dishApiService.pushDishesToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                return response.data.infoList
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("DishRepo", "餐品信息 批量上传失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
    private suspend fun pullDishesFromServer(pullList : List<UpdateInfo>) : DishList {
        val userId = currentUserProfile.value.userId
        val request = UpdateInfoList(infoList = pullList)
        val response = safeApiCall{ RetrofitClient.dishApiService.pullDishesFromServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                val dishListDto = response.data.dishList
                val dishList = mutableListOf<Dish>()
                for (elderDto in dishListDto) {
                    dishList.add(Dish(userId, elderDto))
                }
                return dishList
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("DishRepo", "餐品信息 批量下载失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
}
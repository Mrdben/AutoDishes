package com.mrdben.autodishes.ordergraphs.data

import android.util.Log
import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.UserProfile
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.ordergraphs.api.OrderDto
import com.mrdben.autodishes.ordergraphs.api.OrderListDto
import com.mrdben.autodishes.ordergraphs.api.UpdateInfo
import com.mrdben.autodishes.ordergraphs.api.UpdateInfoList
import com.mrdben.autodishes.utils.getCurrentUtcTime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderRepository(
    private val orderDao: OrderDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    val workState: StateFlow<WorkState> = _workState.asStateFlow()

    val currentUserProfile: StateFlow<UserProfile> = TokenManager.userProfile
    @OptIn(ExperimentalCoroutinesApi::class)
    val orders: StateFlow<OrderList> = currentUserProfile
        .filter { it.userId != 0 }
        .distinctUntilChanged()
        .flatMapLatest {
            orderDao.getAllOrdersFlow(it.userId)
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
                    syncOrderWithServer()
                }
            }
        }
    }

    // ====================== 基础操作 ======================
    fun getOrderById(id: String): Order? {
        return orders.value.find { it.orderId == id }
    }

    fun addOrder(order: Order) {
        ioScope.launch {
            Log.d("OrderRepo","addOrder called: ${order.orderId}")
            val existingOrder = getOrderById(order.orderId)
            if (existingOrder == null) {
                Log.d("OrderRepo","addOrder: No existing. ${order.orderId}")
                val orderToSync = order.copy(
                    userId = currentUserProfile.value.userId,
                    orderId = "order_${System.currentTimeMillis()}",
                    mealTime = getCurrentUtcTime(),
                    syncStatus = SyncStatus.TO_UPLOAD,
                    createdAt = getCurrentUtcTime(),
                    updatedAt = getCurrentUtcTime()
                )
                orderDao.updateOrder(orderToSync)
                syncOrderWithServer()
            }
        }
    }

    fun removeOrder(orderId: String) {
        ioScope.launch {
            val existingOrder = getOrderById(orderId)
            existingOrder?.let {
                orderDao.softDeleteOrder(orderId)
                syncOrderWithServer()
            }
        }
    }
    
    suspend fun syncOrderWithServer() {
        _workState.value = WorkState.Loading
        val userId = currentUserProfile.value.userId
        if (userId == 0) return
        Log.d("OrderRepo","准备同步用餐信息数据到服务器, for user: $userId")
        // from APP to server
        val localOrders = orderDao.getNeedSyncOrders(userId = userId)
        if (localOrders.isEmpty()) {
            Log.d("OrderRepo","没有需要同步的本地用餐信息数据, for user: $userId")
        } else {
            Log.d("OrderRepo", "开始同步用餐信息数据到服务器, 共 ${localOrders.size} 条")
            val serverInfo: List<UpdateInfo> = pushOrdersToServer(localOrders)
            for (info in serverInfo) {
                val order = orderDao.getOrderById(info.orderId)
                if (order == null) {
                    Log.d("OrderRepo", "无法找到用餐信息：${info.orderId}")
                    continue
                }
                val orderToSave = order.copy(
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = info.updatedAt
                )
                orderDao.updateOrder(orderToSave)
                Log.d("OrderRepo", "用餐信息更新成功->: ${order.userId} >>> ${order.orderId} >>> ${orderToSave.updatedAt}。")
            }
            Log.d("OrderRepo", "完成同步用餐信息数据到服务器, for user: $userId")
        }
        Log.d("OrderRepo","准备拉取用餐信息数据到本地, for user: $userId")
        // from server to APP
        val infoList: List<UpdateInfo> = orderDao.getAllOrdersInfo(userId)
        Log.d("OrderRepo","向服务器发送 ${infoList.size} 条请求数据。")
        for (info in infoList) {
            Log.d("OrderRepo","  - 发送成员数据: ${info.orderId} >>> ${info.updatedAt}")
        }
        val updatedOrderList: OrderList = pullOrdersFromServer(infoList)
        Log.d("OrderRepo","服务器返回 ${updatedOrderList.size} 条用餐数据。")
        for (thisOrder in updatedOrderList) {
            orderDao.updateOrder(thisOrder)
            Log.d("OrderRepo", "用餐信息更新成功<-: ${thisOrder.userId} >>> ${thisOrder.orderId} >>> ${thisOrder.updatedAt}。")
        }
        Log.d("OrderRepo","完成拉取用餐信息数据到本地, for user: $userId")
        orderDao.cleanDeletedOrders(userId)
        Log.d("OrderRepo", "用餐信息 本地数据清理完成。")
        _workState.value = WorkState.Idle
    }

    private suspend fun pushOrdersToServer(localOrders: OrderList): List<UpdateInfo> {
        // prepare request info
        val request = OrderListDto(orderList = localOrders.map{OrderDto(it)})
        // server call
        val response = safeApiCall{ RetrofitClient.orderApiService.pushOrdersToServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                return response.data.infoList
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("OrderRepo", "用餐信息 批量上传失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
    private suspend fun pullOrdersFromServer(pullList : List<UpdateInfo>) : OrderList {
        val userId = currentUserProfile.value.userId
        val request = UpdateInfoList(infoList = pullList)
        val response = safeApiCall{ RetrofitClient.orderApiService.pullOrdersFromServer(request) }
        when (response) {
            is ResultWrapper.Success -> {
                return response.data.orderList.map{Order(userId, it)}
            }
            is ResultWrapper.Error -> {
                val code = response.apiError.statusCode
                val message = response.apiError.message
                Log.e("OrderRepo", "用餐信息 批量下载失败。 错误码: $code, 错误信息: $message")
                return emptyList()
            }
        }
    }
}
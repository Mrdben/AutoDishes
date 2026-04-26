package com.mrdben.autodishes.ordergraphs.data

import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.familygraphs.data.DEFAULT_FAMILY
import com.mrdben.autodishes.familygraphs.data.Family
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 订单数据仓库
 * 负责管理所有 Order 数据，纯逻辑层，与 Compose/UI 无关
 */
class OrderRepository(
    private val orderDao: OrderDao
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // 菜品 Flow（实时更新）
    val orders: StateFlow<OrderList> = orderDao.getAllOrdersFlow()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    // ====================== 基础操作 ======================
    fun getOrderById(id: String): Order? {
        return orders.value.find { it.orderId == id }
    }
    /**
     * 添加一个订单
     */
    fun addOrder(order: Order) {
        ioScope.launch {
            val existingOrder = getOrderById(order.orderId)
            if (existingOrder == null) {
                orderDao.insertOrder(order)
            }
        }
    }

    /**
     * 删除指定订单
     */
    fun removeOrder(orderId: String) {
        ioScope.launch {
            orderDao.deleteOrderById(orderId)
        }
    }

//    private fun createTestOrderData() : OrderList {
//        return listOf (
//            Order(
//                orderId = "order_001",
//                createTime = 1744008000000,
//                mealType = MealType.BREAKFAST,
//                elderIds = listOf("debug_elder_002", "debug_elder_004"),
//                dishIds = listOf("dish_002", "dish_003", "dish_004")
//            ),
//            Order(
//                orderId = "order_002",
//                createTime = 1744089600000,
//                mealType = MealType.LUNCH,
//                elderIds = listOf("debug_elder_001", "debug_elder_003"),
//                dishIds = listOf("dish_001", "dish_003", "dish_004")
//            )
//            ,
//            Order(
//                orderId = "order_003",
//                createTime = 1744168000000,
//                mealType = MealType.DINNER,
//                elderIds = listOf("debug_elder_001", "debug_elder_002", "debug_elder_003"),
//                dishIds = listOf("dish_001", "dish_002", "dish_003", "dish_004")
//            )
//        )
//    }
}
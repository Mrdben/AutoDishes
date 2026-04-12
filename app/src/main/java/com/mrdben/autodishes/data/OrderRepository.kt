package com.mrdben.autodishes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 订单数据仓库
 * 负责管理所有 Order 数据，纯逻辑层，与 Compose/UI 无关
 */
class OrderRepository {
    companion object {
        val instance: OrderRepository by lazy {
            OrderRepository()
        }
    }
    // 内部存储订单列表（线程安全）
    private val _orderList = MutableStateFlow<OrderList>(createTestOrderData())
    val orderList: StateFlow<OrderList> = _orderList.asStateFlow()

    // ====================== 基础操作 ======================
    fun getOrderById(id: String): Order? {
        return _orderList.value.find { it.orderId == id }
    }
    /**
     * 添加一个订单
     */
    fun addOrder(order: Order) {
        val currentList = _orderList.value.toMutableList()
        currentList.add(order)
        _orderList.value = currentList
    }


    /**
     * 删除指定订单
     */
    fun removeOrder(orderId: String) {
        val currentList = _orderList.value.toMutableList()
        currentList.removeIf{ it.orderId == orderId }
        _orderList.value = currentList
    }

    /**
     * 清空所有订单
     */
    fun clearAllOrders() {
        _orderList.value = emptyList()
    }
    private fun createTestOrderData() : OrderList {
        return listOf (
            Order(
                orderId = "order_001",
                createTime = 1744008000000,
                mealType = MealType.BREAKFAST,
                elderIds = listOf("debug_elder_002","debug_elder_004"),
                dishIds = listOf("dish_002","dish_003","dish_004")
            ),
            Order(
                orderId = "order_002",
                createTime = 1744089600000,
                mealType = MealType.LUNCH,
                elderIds = listOf("debug_elder_001","debug_elder_003"),
                dishIds = listOf("dish_001","dish_003","dish_004")
            )
            ,
            Order(
                orderId = "order_003",
                createTime = 1744168000000,
                mealType = MealType.DINNER,
                elderIds = listOf("debug_elder_001","debug_elder_002","debug_elder_003"),
                dishIds = listOf("dish_001","dish_002","dish_003","dish_004")
            )
        )
    }
}
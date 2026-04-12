package com.mrdben.autodishes.ordergraphs.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewOrderRepository {
    companion object {
        val instance: NewOrderRepository by lazy {
            NewOrderRepository()
        }
    }
    //private val _newOrder = MutableStateFlow<Order?>(createTestOrderData())
    private val _newOrder = MutableStateFlow<Order?>(null)
    val newOrder: StateFlow<Order?> = _newOrder.asStateFlow()

    fun createNewOrder() {
        if (_newOrder.value == null) {
            val timeStamp = System.currentTimeMillis()
            _newOrder.value = Order(
                orderId = "order_${timeStamp}",
                createTime = timeStamp,
                mealType = MealType.UNKNOWN,
                elderIds = emptyList(),
                dishIds = emptyList()
            )
        }
    }
    fun setMealType(type : MealType) {
        _newOrder.value?.let { currentOrder ->
            _newOrder.value = currentOrder.copy(mealType = type)
        }
    }
    fun getElderIdList() : List<String> {
        return _newOrder.value?.elderIds?: emptyList()
    }
    fun getDishIdList() : List<String> {
        return _newOrder.value?.dishIds?: emptyList()
    }
    fun setElderIdList(elderList : List<String>) {
        _newOrder.value?.let { currentOrder ->
            _newOrder.value = currentOrder.copy(elderIds = elderList)
        }
    }
    fun setDishIdList(dishList : List<String>) {
        _newOrder.value?.let { currentOrder ->
            _newOrder.value = currentOrder.copy(dishIds = dishList)
        }
    }

    fun clearOrder() {
        _newOrder.value = null
    }

//    private fun createTestOrderData() : Order {
//        return Order(
//            orderId = "order_1744008000000",
//            createTime = 1744008000000,
//            mealType = MealType.BREAKFAST,
//            elderIds = listOf("debug_elder_002","debug_elder_004"),
//            dishIds = listOf("dish_002","dish_003","dish_004")
//        )
//    }
}
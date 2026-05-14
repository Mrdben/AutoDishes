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
    private val _newOrder = MutableStateFlow(DEFAULT_ORDER)
    val newOrder: StateFlow<Order> = _newOrder.asStateFlow()

    fun setMealType(type : MealType) {
        _newOrder.value = _newOrder.value.copy(mealType = type)
    }
    fun getElderIdList() : List<String> {
        return _newOrder.value.elderIds
    }
    fun getDishIdList() : List<String> {
        return _newOrder.value.dishIds
    }
    fun setElderIdList(elderList : List<String>) {
        _newOrder.value = _newOrder.value.copy(elderIds = elderList)
    }
    fun setDishIdList(dishList : List<String>) {
        _newOrder.value = _newOrder.value.copy(dishIds = dishList)
    }

    fun clearOrder() {
        _newOrder.value = DEFAULT_ORDER
    }
}
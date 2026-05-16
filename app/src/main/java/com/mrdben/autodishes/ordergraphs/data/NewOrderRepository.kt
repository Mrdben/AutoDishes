package com.mrdben.autodishes.ordergraphs.data

import com.mrdben.autodishes.dishgraphs.data.DishList
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

    private val _tempDishes = MutableStateFlow<DishList>(emptyList())
    val tempDishes: StateFlow<DishList> = _tempDishes.asStateFlow()

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

    fun initialize(mealType: MealType, elderIds: List<String>, dishIds: List<String>) {
        // full in new value only if the structure is empty
        val order = _newOrder.value
        if (order.mealType == MealType.UNKNOWN
            && order.elderIds.isEmpty()
            && order.dishIds.isEmpty()) {
            _newOrder.value = DEFAULT_ORDER.copy(
                mealType = mealType,
                elderIds = elderIds,
                dishIds = dishIds
            )
        }
    }

    fun setTempDishes( tempDishes: DishList) {
        _tempDishes.value = tempDishes
    }
}
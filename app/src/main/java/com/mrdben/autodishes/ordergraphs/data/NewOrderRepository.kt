package com.mrdben.autodishes.ordergraphs.data

import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.ordergraphs.aichef.AiChef
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
    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    val workState: StateFlow<WorkState> = _workState.asStateFlow()
    private var aiChef: AiChef? = null
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
    fun setupAiChef(elders: ElderList,
                    mealType: MealType,
                    dishRepo: DishRepository,
                    orderRepo: OrderRepository) {
        aiChef = AiChef(elders, mealType, dishRepo, orderRepo)
    }
    fun cleanupAiChef() {
        aiChef = null
        _tempDishes.value = emptyList()
    }
    suspend fun getDishesFromAI() {
        _workState.value = WorkState.Loading
        _tempDishes.value = aiChef?.getAiChefDishes() ?: emptyList()
        _workState.value = WorkState.Idle
    }
}
package com.mrdben.autodishes.ordergraphs.data

import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
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
    private val _tempDishIdList = MutableStateFlow<List<String>>(emptyList())
    val tempDishIdList = _tempDishIdList.asStateFlow()
    private var aiChef: AiChef? = null
    fun getTempDishById(id: String): Dish? {
        return _tempDishes.value.find { it.dishId == id }
    }
    fun setMealType(type : MealType) {
        _newOrder.value = _newOrder.value.copy(mealType = type)
    }
    fun setElderIdList(elderList : List<String>) {
        _newOrder.value = _newOrder.value.copy(elderIds = elderList)
    }
    fun setDishIdList(dishList : List<String>) {
        _newOrder.value = _newOrder.value.copy(dishIds = dishList)
    }
    fun setTempDishIdList(tempDishList: List<String>) {
        _tempDishIdList.value = tempDishList
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
    ///////////////////////////////////////// Dish list from AI chat
    fun setupAiChef(familyRepository: FamilyRepository,
                    dishRepository: DishRepository,
                    orderRepository: OrderRepository,
                    userPrompt: String
    ) {
        aiChef = AiChef(familyRepository,
                        dishRepository,
                        orderRepository,
                        instance,
                        userPrompt)
    }
    fun cleanupAiChef() {
        aiChef = null
        _tempDishes.value = emptyList()
        _tempDishIdList.value = emptyList()
    }
    suspend fun getDishesFromAI() {
        // update UI and wait from server to response, may take up to 20 secs
        val newAIDishes: DishList = aiChef?.getAiChefDishes() ?: emptyList()
        // 从AI推荐的菜品列表中，选择不重名的菜品，加入tempDishes列表
        _tempDishes.value += newAIDishes.filter { noDishWithSameName(_tempDishes.value, it)}
    }

    private fun noDishWithSameName(dishList: DishList, newDish: Dish) : Boolean {
        return dishList.find{it.name == newDish.name} == null
    }
}
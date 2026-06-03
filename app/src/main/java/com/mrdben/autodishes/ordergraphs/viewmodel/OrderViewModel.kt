package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.OrderList
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.dishgraphs.data.IngredientCategory
import com.mrdben.autodishes.dishgraphs.data.getDisplayName

class OrderViewModel(
    private val familyRepo : FamilyRepository,
    private val dishRepo : DishRepository,
    private val orderRepo : OrderRepository
) : ViewModel() {
    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    val workState: StateFlow<WorkState> = _workState.asStateFlow()
    var currentFilterType = MutableStateFlow(MealType.UNKNOWN)
    var currentSortType = MutableStateFlow(SortOrder.DESC)
    var searchKeyword = MutableStateFlow("")

    private val _currentOrderList = MutableStateFlow<OrderList>(emptyList())
    val currentOrderList: StateFlow<OrderList> = _currentOrderList.asStateFlow()

    init {
        // 监听仓库数据变化 → 自动刷新
        viewModelScope.launch {
            orderRepo.orders.collect {
                refreshList()
            }
        }
    }
    private fun refreshList() {
        var list = orderRepo.orders.value
        // 筛选和搜索
        if (currentFilterType.value != MealType.UNKNOWN) {
            list = list.filter {
                it.mealType == currentFilterType.value  //筛选
            }
        }
        if (searchKeyword.value.isNotBlank()) {
            val key = searchKeyword.value.lowercase()
            list = list.filter {
                it.elderIds.any { elderId ->
                    val elder = getElderById(elderId)
                    elder != null && elder.name.lowercase().contains(key)
                }
                ||
                it.dishIds.any { dishId ->
                    val dish = dishRepo.getDishById(dishId)
                    dish != null && dish.name.lowercase().contains(key)
                }
            }
        }
        // 排序
        list = if (currentSortType.value == SortOrder.ASC) {
            list.sortedBy{ it.mealTime }
        } else {
            list.sortedByDescending{ it.mealTime }
        }
        // 3. 刷新页面
        _currentOrderList.value = list
    }
    fun search(keyword: String) {
        searchKeyword.value = keyword
        refreshList()
    }

    // ====================== 清空搜索 ======================
    fun setFilter(filter: MealType) {
        currentFilterType.value = filter
        refreshList()
    }

    fun clearFilterAndSearch() {
        searchKeyword.value = ""
        currentFilterType.value = MealType.UNKNOWN
        refreshList()
    }
    fun setSortType(sort: SortOrder) {
        currentSortType.value = sort
        refreshList()
    }

    /**
     * 删除订单
     */
    fun removeOrderById(orderId: String) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (orderRepo.removeOrder(orderId))
                orderRepo.syncOrderWithServer()
            _workState.value = WorkState.Idle
        }
    }

    fun getElderNameList(elderIds: List<String>) : List<String> {
        return elderIds.mapNotNull { elderId ->
            familyRepo.getElderById(elderId)?.name
        }
    }

    fun getDishNameList(dishIds: List<String>) : List<String> {
        return dishIds.mapNotNull { dishIds ->
            dishRepo.getDishById(dishIds)?.name
        }
    }

    fun getOrderById(id: String): Order? {
        return orderRepo.getOrderById(id)
    }
    fun getElderById(elderId: String): Elder? {
        return familyRepo.getElderById(elderId)
    }
    fun getDishById(id: String): Dish? {
        return dishRepo.getDishById(id)
    }
    fun refreshOrderList() {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            orderRepo.syncOrderWithServer()
            _workState.value = WorkState.Idle
        }
    }

    fun generateShoppingList(currentOrder: Order) : List<Map<String, String>> {
        val dishList = currentOrder.dishIds.mapNotNull { getDishById(it) }

        // 1. 定义单位到基础单位(g/ml)的转换比例
        val unitToBase = mapOf( "g" to Pair(1.0, "g"),
                                "kg" to Pair(1000.0, "g"),
                                "ml" to Pair(1.0, "ml"),
                                "L" to Pair(1000.0, "ml"),
                                "克" to Pair(1.0, "g"),
                                "千克" to Pair(1000.0, "g"),
                                "斤" to Pair(500.0, "g"),
                                "市斤" to Pair(1000.0, "g"),
                                "两" to Pair(50.0, "g"),
                                "升" to Pair(1000.0, "ml"),
                                "毫升" to Pair(1.0, "ml")
        )
        data class Amount(var totalAmount: Double,
                          var standardUnit: String,
                          var owner: String,
                          var category: IngredientCategory)
        val ingredientMap = mutableMapOf<String, Amount>()
        dishList.forEach { dish ->
            dish.ingredients.forEach { ing ->
                // 转换为基准单位的值
                val baseValue = ing.amount * (unitToBase[ing.unit]?.first ?: 1.0)
                val baseUnit = unitToBase[ing.unit]?.second ?: ing.unit
                val baseString = if (ing.amount == 0.0) " ${dish.name}->未包含"
                                 else " ${dish.name}->${formattedAmount(baseValue)}$baseUnit"
                // 如果该原料已存在，则累加数值；否则初始化
                if (ingredientMap.containsKey(ing.name)) {
                    ingredientMap[ing.name]!!.totalAmount += baseValue
                    ingredientMap[ing.name]!!.standardUnit.ifBlank { baseUnit }
                    ingredientMap[ing.name]!!.owner += baseString
                    if (ingredientMap[ing.name]!!.category == IngredientCategory.OTHER)
                        ingredientMap[ing.name]!!.category = ing.category
                } else {
                    ingredientMap[ing.name] = Amount(
                        baseValue,
                        baseUnit,
                        baseString,
                        ing.category
                    )
                }
            }
        }
        // 4. 将聚合结果格式化为最终的 List<Map<String, String>>
        val result = mutableListOf<Map<String, String>>()
        for ((name, amount) in ingredientMap) {
            result.add(mapOf(
                "name" to name,
                "amount" to formattedAmount(amount.totalAmount),
                "unit" to amount.standardUnit,
                "owner" to amount.owner.trim()
                    .replace(" ","、"),
                "category" to amount.category.getDisplayName()
            ))
        }
        return result
    }
    private fun formattedAmount(amount: Double) : String {
        return if (amount == 0.0) "若干"
               else {if (amount == amount.toLong().toDouble())  amount.toLong().toString()
                     else amount.toString()}
    }
}
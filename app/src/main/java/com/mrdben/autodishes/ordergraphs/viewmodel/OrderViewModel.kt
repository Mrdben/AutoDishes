package com.mrdben.autodishes.ordergraphs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.mrdben.autodishes.data.DEFAULT_ORDER
import com.mrdben.autodishes.data.Dish
import com.mrdben.autodishes.data.DishRepository
import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.FamilyRepository
import com.mrdben.autodishes.data.MealType
import com.mrdben.autodishes.data.Order
import com.mrdben.autodishes.data.OrderList
import com.mrdben.autodishes.data.OrderRepository
import com.mrdben.autodishes.data.SortOrder

class OrderViewModel : ViewModel() {
    // 初始化仓库
    private val repository = OrderRepository.instance
    private val familyRepo = FamilyRepository.instance
    private val dishRepo = DishRepository.instance
    var currentFilterType = MutableStateFlow(MealType.UNKNOWN)
    var currentSortType = MutableStateFlow(SortOrder.DESC)
    var searchKeyword = MutableStateFlow("")

    private val _currentOrderList = MutableStateFlow<OrderList>(emptyList())
    val currentOrderList: StateFlow<OrderList> = _currentOrderList.asStateFlow()

    init {
        // 监听仓库数据变化 → 自动刷新
        viewModelScope.launch {
            repository.orderList.collect {
                refreshList()
            }
        }
    }
    private fun refreshList() {
        var list = repository.orderList.value
        // 筛选和搜索
        if (currentFilterType.value != MealType.UNKNOWN || searchKeyword.value.isNotBlank()) {
            val key = searchKeyword.value.lowercase()
            list = list.filter {
                it.mealType == currentFilterType.value  //筛选
                && ( //搜索
                    it.elderIds.any { elderId ->
                        val elder = familyRepo.getElderById(elderId)
                        elder != null && elder.name.lowercase().contains(key)
                    }
                    ||
                    it.dishIds.any { dishId ->
                        val dish = dishRepo.getDishById(dishId)
                        dish != null && dish.name.lowercase().contains(key)
                    }
                )
            }
        }
        // 排序
        list = if (currentSortType.value == SortOrder.ASC) {
            list.sortedBy{ it.createTime }
        } else {
            list.sortedByDescending{ it.createTime }
        }
        // 3. 刷新页面
        _currentOrderList.value = list
    }
    fun search(keyword: String) {
        searchKeyword.value = keyword
        refreshList()
    }

    // ====================== 清空搜索 ======================
    fun clearSearch() {
        searchKeyword.value = ""
        refreshList()
    }
    fun setFilter(filter: MealType) {
        currentFilterType.value = filter
        refreshList()
    }
    fun clearFilter() {
        currentFilterType.value = MealType.UNKNOWN
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
        repository.removeOrder(orderId)
    }

    /**
     * 清空所有订单
     */
    fun clearAllOrders() {
        repository.clearAllOrders()
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
        return repository.getOrderById(id)
    }
    fun getElderById(id: String): Elder? {
        return familyRepo.getElderById(id)
    }
    fun getDishById(id: String): Dish? {
        return dishRepo.getDishById(id)
    }
}
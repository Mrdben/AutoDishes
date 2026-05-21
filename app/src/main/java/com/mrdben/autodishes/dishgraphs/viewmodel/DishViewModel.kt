package com.mrdben.autodishes.dishgraphs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishList
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.data.DishSortType
import com.mrdben.autodishes.ordergraphs.data.NewOrderRepository
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ordergraphs.data.OrderRepository

class DishViewModel(
    private val dishRepository : DishRepository,
) : ViewModel() {
    private val newOrderRepository = NewOrderRepository.instance
    private val _workState = MutableStateFlow<WorkState>(WorkState.Idle)
    val workState: StateFlow<WorkState> = _workState.asStateFlow()
    private var currentSortType: DishSortType = DishSortType.NAME
    private var currentSortOrder: SortOrder = SortOrder.ASC
    private var useTempDishes: Boolean = false
    val currentSortState = MutableStateFlow(
        Pair(currentSortType, currentSortOrder)
    )
    var searchKeyword = MutableStateFlow("")

    private val _currentDishList = MutableStateFlow<DishList>(emptyList())
    val currentDishList: StateFlow<DishList> = _currentDishList.asStateFlow()

    init {        // 监听仓库数据变化 → 自动刷新
        viewModelScope.launch {
            dishRepository.dishes.collect {
                refreshList()
            }
        }
        viewModelScope.launch {
            newOrderRepository.tempDishes.collect {
                refreshList()
            }
        }
    }
    private fun refreshList() {
        var list = if (useTempDishes) newOrderRepository.tempDishes.value
                   else dishRepository.dishes.value

        // 1. 搜索过滤
        if (searchKeyword.value.isNotBlank()) {
            val key = searchKeyword.value.lowercase()
            list = list.filter {
                it.name.lowercase().contains(key)||
                it.ingredients.lowercase().contains(key)
            }
        }

        // 2. 排序
        val comparator = currentSortType.getComparator()
        list = if (currentSortOrder == SortOrder.ASC) {
            list.sortedWith(comparator)
        } else {
            list.sortedWith(comparator.reversed())
        }

        // 3. 刷新页面
        _currentDishList.value = list
    }

    // ====================== 搜索 ======================
    fun search(keyword: String) {
        searchKeyword.value = keyword
        refreshList()
    }

    // ====================== 清空搜索 ======================
    fun clearSearch() {
        searchKeyword.value = ""
        refreshList()
    }

    fun toggleSortType(sortType: DishSortType) {
        if (currentSortType == sortType) {
            // 点击当前激活的排序→切换顺序
            currentSortOrder = currentSortOrder.toggle()
        } else {
            // 点击新的排序→重置为升序
            currentSortType = sortType
            currentSortOrder = SortOrder.ASC
        }
        // 更新排序状态+重新排序
        currentSortState.value = Pair(currentSortType, currentSortOrder)
        refreshList()
    }
    // ======================
    // 对外暴露核心方法（直接调用仓库）
    // ======================

    // 2. 添加菜品
    fun addDish(dish: Dish) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (dishRepository.addDish(dish))
                dishRepository.syncDishWithServer()
            _workState.value = WorkState.Idle
        }
    }

    // 3. 根据ID删除菜品
    fun removeDishById(id: String) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (dishRepository.removeDishById(id))
                dishRepository.syncDishWithServer()
            _workState.value = WorkState.Idle
        }
    }

    // 4. 更新整个菜品
    fun updateDish(updatedDish: Dish) {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            dishRepository.updateDish(updatedDish)
            dishRepository.syncDishWithServer()
            _workState.value = WorkState.Idle
        }
    }

    // 5. 根据ID获取单个菜品
    fun getDishById(id: String): Dish? {
        return dishRepository.getDishById(id) ?: newOrderRepository.getTempDishById(id)
    }

    // 6. 更新喜欢程度（favoriteLevel）
    fun like(id: String, delta: Int = 1) {
        viewModelScope.launch {
            dishRepository.like(id, delta)
        }
    }

    fun getDishSelectionList() : List<String> {
        return if (useTempDishes) newOrderRepository.tempDishIdList.value
               else newOrderRepository.newOrder.value.dishIds
    }

    fun setDishSelectionList(idList : List<String>) {
        if (useTempDishes) newOrderRepository.setTempDishIdList(idList.distinct())
        else newOrderRepository.setDishIdList(idList.distinct())
    }

    fun isDishSelected(dishId: String): Boolean {
        return dishId in getDishSelectionList()
    }
    fun changeDishSelection(dishId: String, isSelected: Boolean = false) {
        val oldList = getDishSelectionList()
        val newList = if (isSelected) oldList - dishId
                      else oldList + dishId
        setDishSelectionList(newList.distinct())
    }

    fun refreshDishList() {
        viewModelScope.launch {
            _workState.value = WorkState.Loading
            if (useTempDishes) newOrderRepository.getDishesFromAI()
            else dishRepository.syncDishWithServer()
            _workState.value = WorkState.Idle
        }
    }

    fun setUseTempDishes(useTemp: Boolean) {
        useTempDishes = useTemp
        refreshList()
    }
    fun setUseTempDishes(dishId: String) {
        useTempDishes = newOrderRepository.tempDishes.value.find{ it.dishId == dishId } != null
        refreshList()
    }

    fun isDishListEmpty(): Boolean {
        return if (useTempDishes) newOrderRepository.tempDishes.value.isEmpty()
        else dishRepository.dishes.value.isEmpty()
    }
}
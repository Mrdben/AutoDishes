package com.mrdben.autodishes.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DishViewModel : ViewModel() {

    private val repository = DishRepository.instance
    private var currentSortType: DishSortType = DishSortType.NAME
    private var currentSortOrder: SortOrder = SortOrder.ASC
    val currentSortState = MutableStateFlow(
        Pair(currentSortType, currentSortOrder)
    )
    var searchKeyword = MutableStateFlow("")
    private val _currentDishList = MutableStateFlow<DishList>(emptyList())
    val currentDishList: StateFlow<DishList> = _currentDishList.asStateFlow()

    init {
        // 监听仓库数据变化 → 自动刷新
        viewModelScope.launch {
            repository.currentDishList.collect {
                refreshList()
            }
        }
    }
    private fun refreshList() {
        var list = repository.currentDishList.value

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

    // 1. 更新整个列表
    fun updateEntireList(newList: DishList) {
        repository.updateList(newList)
    }

    // 2. 添加菜品
    fun addDish(dish: Dish) {
        repository.addDish(dish)
    }

    // 3. 根据ID删除菜品
    fun removeDishById(id: String) {
        repository.removeDishById(id)
    }

    // 4. 更新整个菜品
    fun updateDish(updatedDish: Dish) {
        repository.updateDish(updatedDish)

    }

    // 5. 根据ID获取单个菜品
    fun getDishById(id: String): Dish? {
        return repository.getDishById(id)
    }

    // 6. 更新喜欢程度（favoriteLevel）
    fun like(id: String) {
        repository.like(id)
    }

    fun disLike(id: String) {
        repository.disLike(id)
    }
}
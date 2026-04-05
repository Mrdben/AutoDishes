package com.mrdben.autodishes.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishViewModel : ViewModel() {

    private val repository = DishRepository.instance
    private var currentSortType: DishSortType = DishSortType.NAME
    private var currentSortOrder: SortOrder = SortOrder.ASC
    val currentSortState = MutableStateFlow(
        Pair(currentSortType, currentSortOrder)
    )
    private val _currentDishList = MutableStateFlow(repository.currentDishList.value)
    val currentDishlist: StateFlow<DishList> = _currentDishList.asStateFlow()

    init {
        // 3. 监听 Repository 的原始数据变化（名称/老人列表变化）
        viewModelScope.launch {
            repository.currentDishList.collect { originalDishList ->
                updateSortedDishList(originalDishList)
            }
        }
    }
    private fun updateSortedDishList(originalDishList: DishList) {
        val comparator = currentSortType.getComparator()
        val sortedDishList = if (currentSortOrder == SortOrder.ASC) {
            originalDishList.sortedWith(comparator)
        } else {
            originalDishList.sortedWith(comparator.reversed()) // 逆序
        }
        _currentDishList.value = sortedDishList
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
        updateSortedDishList(repository.currentDishList.value)
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
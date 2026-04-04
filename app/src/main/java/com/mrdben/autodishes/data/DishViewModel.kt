package com.mrdben.autodishes.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DishViewModel : ViewModel() {

    private val repository = DishRepository.instance
    val currentDishView: StateFlow<DishList> = repository.currentDishList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = defaultDishList
        )

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
    fun deleteDishById(id: String) {
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
    fun updateFavoriteLevel(id: String, level: Int) {
        repository.updateFavoriteLevel(id, level)
    }
}
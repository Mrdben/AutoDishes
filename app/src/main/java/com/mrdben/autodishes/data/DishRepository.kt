package com.mrdben.autodishes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DishRepository {
    companion object {
        val instance: DishRepository by lazy {
            DishRepository()
        }
    }

    // 内部可修改的菜品列表（用 StateFlow 方便 Compose 观察变化）
    private val _currentDishList = MutableStateFlow<DishList>(defaultDishList)

    // 外部只读（Compose 直接用）
    val currentDishList: StateFlow<DishList> = _currentDishList.asStateFlow()

    /** 1. 直接替换整个 DishList */
    fun updateList(newList: DishList) {
            _currentDishList.value = newList
    }

    /** 2. 添加一个 Dish */
    fun addDish(dish: Dish) {
        _currentDishList.value = _currentDishList.value + dish
    }

    /** 3. 根据 ID 删除一个 Dish */
    fun removeDishById(id: String) {
        _currentDishList.value = _currentDishList.value.filter { it.dishId != id }
    }

    /** 4. 根据 ID 更新整个 Dish 对象 */
    fun updateDish(updatedDish: Dish) {
        _currentDishList.value = _currentDishList.value.map {
            if (it.dishId == updatedDish.dishId) updatedDish else it
        }
    }

    /** 5. 根据 ID 获取单个 Dish（找不到返回 null） */
    fun getDishById(id: String): Dish? {
        return _currentDishList.value.find { it.dishId == id }
    }

    fun updateFavoriteLevel(id: String, level: Int) {
        _currentDishList.value = _currentDishList.value.map {
            if (it.dishId == id) it.copy(favoriteLevel = level) else it
        }
    }
}
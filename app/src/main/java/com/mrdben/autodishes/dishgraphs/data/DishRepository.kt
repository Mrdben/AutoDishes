package com.mrdben.autodishes.dishgraphs.data

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
    private val _currentDishList = MutableStateFlow(createTestDishData())

    // 外部只读（Compose 直接用）
    val currentDishList: StateFlow<DishList> = _currentDishList.asStateFlow()

    /** 2. 添加一个 Dish */
    fun addDish(dish: Dish) {
        _currentDishList.value += dish
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

    fun like(id: String) {
        _currentDishList.value = _currentDishList.value.map {
            val level = (it.favoriteLevel + 1).coerceAtMost(10)
            if (it.dishId == id) it.copy(favoriteLevel = level) else it
        }
    }

    fun disLike(id: String) {
        _currentDishList.value = _currentDishList.value.map {
            val level = (it.favoriteLevel - 1).coerceAtLeast(0)
            if (it.dishId == id) it.copy(favoriteLevel = level) else it
        }
    }

    private fun createTestDishData(): DishList {
        return listOf(
            Dish(
                dishId = "dish_001",
                name = "番茄炒蛋",
                taste = "酸甜",
                type = DishType.WARM_VEGGIE,
                cuisine = CuisineType.FAMILY,
                ingredients = "番茄、鸡蛋、盐、糖",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
                favoriteLevel = 3
            ),
            Dish(
                dishId = "dish_002",
                name = "乾隆白菜",
                taste = "鲜香",
                type = DishType.COLD_VEGGIE,
                cuisine = CuisineType.BEIJING,
                ingredients = "番茄、鸡蛋、盐、糖",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
                favoriteLevel = 1
            ),
            Dish(
                dishId = "dish_003",
                name = "锅包肉",
                taste = "酸甜",
                type = DishType.WARM_MEAT,
                cuisine = CuisineType.DONGBEI,
                ingredients = "番茄、鸡蛋、盐、糖、白菜",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
                favoriteLevel = 5
            ),
            Dish(
                dishId = "dish_004",
                name = "开水白菜",
                taste = "淡雅",
                type = DishType.SOUP,
                cuisine = CuisineType.CANTONESE,
                ingredients = "番茄、鸡蛋、盐、糖",
                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
                favoriteLevel = 5
            )

        )
    }
}
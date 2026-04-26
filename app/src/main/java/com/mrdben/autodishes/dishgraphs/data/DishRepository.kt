package com.mrdben.autodishes.dishgraphs.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishRepository (
    private val dishDao: DishDao
){
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // 菜品 Flow（实时更新）
    val dishes: StateFlow<DishList> = dishDao.getAllDishesFlow()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            initialValue = emptyList(),
            started = SharingStarted.Eagerly
        )

    // 初始化：如果需要默认数据可以在这里加
//    init {
//        ioScope.launch {
//            if (dishDao.getAllDishesFirst().isEmpty()) {
//                 dishDao.insertDish(DEFAULT_DISH)
//            }
//        }
//    }

    /** 2. 添加一个 Dish */
    fun addDish(dish: Dish) {
        ioScope.launch {
            val existingDish = getDishById(dish.dishId)
            if (existingDish == null) {
                dishDao.insertDish(dish)
            }
        }
    }

    /** 3. 根据 ID 删除一个 Dish */
    fun removeDishById(dishId: String) {
        ioScope.launch {
            dishDao.deleteDishById(dishId)
        }
    }

    /** 4. 根据 ID 更新整个 Dish 对象 */
    fun updateDish(updatedDish: Dish) {
        ioScope.launch {
            dishDao.updateDish(updatedDish)
        }
    }

    /** 5. 根据 ID 获取单个 Dish（找不到返回 null） */
    fun getDishById(id: String): Dish? {
        return dishes.value.find { it.dishId == id }
    }

    fun like(id: String) {
        ioScope.launch {
            val dish = getDishById(id)
            if (dish!=null) {
                val newLevel = (dish.favoriteLevel + 1).coerceAtMost(10)
                dishDao.updateDish(dish.copy(favoriteLevel = newLevel))
            }
        }
    }

    fun disLike(id: String) {
        ioScope.launch {
            val dish = getDishById(id)
            if (dish!=null) {
                val newLevel = (dish.favoriteLevel - 1).coerceAtLeast(10)
                dishDao.updateDish(dish.copy(favoriteLevel = newLevel))
            }
        }
    }

//    private fun createTestDishData(): DishList {
//        return listOf(
//            Dish(
//                dishId = "dish_001",
//                name = "番茄炒蛋",
//                taste = "酸甜",
//                type = DishType.WARM_VEGGIE,
//                cuisine = CuisineType.FAMILY,
//                ingredients = "番茄、鸡蛋、盐、糖",
//                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
//                favoriteLevel = 3
//            ),
//            Dish(
//                dishId = "dish_002",
//                name = "乾隆白菜",
//                taste = "鲜香",
//                type = DishType.COLD_VEGGIE,
//                cuisine = CuisineType.BEIJING,
//                ingredients = "番茄、鸡蛋、盐、糖",
//                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
//                favoriteLevel = 1
//            ),
//            Dish(
//                dishId = "dish_003",
//                name = "锅包肉",
//                taste = "酸甜",
//                type = DishType.WARM_MEAT,
//                cuisine = CuisineType.DONGBEI,
//                ingredients = "番茄、鸡蛋、盐、糖、白菜",
//                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
//                favoriteLevel = 5
//            ),
//            Dish(
//                dishId = "dish_004",
//                name = "开水白菜",
//                taste = "淡雅",
//                type = DishType.SOUP,
//                cuisine = CuisineType.CANTONESE,
//                ingredients = "番茄、鸡蛋、盐、糖",
//                recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅",
//                favoriteLevel = 5
//            )
//
//        )
//    }
}
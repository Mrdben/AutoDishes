package com.mrdben.autodishes.data

data class Dish(
    val dishId: String,
    val name: String,
    val taste: String,
    val ingredients: String,
    val recipe: String,
    val favoriteLevel: Int = 0
)

typealias DishList = List<Dish>

val DEFAULT_DISH = Dish(
    dishId = "",
    name = "番茄炒蛋",
    taste = "酸甜",
    ingredients = "番茄、鸡蛋、盐、糖",
    recipe = "1. 炒蛋 2. 炒番茄 3. 混合出锅"
)

// 测试用示例数据
val defaultDishList = listOf(
    DEFAULT_DISH
)
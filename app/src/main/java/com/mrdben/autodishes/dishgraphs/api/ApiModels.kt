package com.mrdben.autodishes.dishgraphs.api

import com.google.gson.annotations.SerializedName
import com.mrdben.autodishes.dishgraphs.data.CuisineType
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishType

data class UpdateInfo(
    @SerializedName("dish_id")
    val dishId: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UpdateInfoList(
    @SerializedName("info_list")
    val infoList: List<UpdateInfo> = emptyList()
)

data class DishDto(
    @SerializedName("dish_id")
    val dishId: String,
    val name: String,
    val taste: String,
    @SerializedName("dish_type")  // "type" in python is a keyword
    val type: DishType,
    val cuisine: CuisineType = CuisineType.FAMILY,
    val ingredients: String,
    val recipe: String,
    @SerializedName("favorite_level")
    val favoriteLevel: Int = 0,
    val comments: String,
    // 同步字段（必须和服务端对应）
    @SerializedName("is_deleted")
    val isDeleted: Boolean = false,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    constructor(
        dish : Dish
    ) : this (
        dishId = dish.dishId,
        name = dish.name,
        taste = dish.taste,
        type = dish.type,
        cuisine = dish.cuisine,
        ingredients = dish.ingredients,
        recipe = dish.recipe,
        favoriteLevel = dish.favoriteLevel,
        comments = dish.comments,
        isDeleted = dish.isDeleted,
        updatedAt = dish.updatedAt,
        createdAt = dish.createdAt
    )
}

data class DishListDto(
    @SerializedName("dish_list")
    val dishList: List<DishDto> = emptyList()
)
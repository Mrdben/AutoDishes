package com.mrdben.autodishes.ordergraphs.api

import com.google.gson.annotations.SerializedName
import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.utils.getCurrentUtcTime

data class UpdateInfo(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UpdateInfoList(
    @SerializedName("info_list")
    val infoList: List<UpdateInfo> = emptyList()
)

data class OrderDto(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("meal_type")
    val mealType: MealType,
    @SerializedName("meal_time")
    val mealTime: String = getCurrentUtcTime(),
    @SerializedName("elder_ids")
    val elderIds: List<String> = emptyList(),
    @SerializedName("dish_ids")
    val dishIds: List<String> = emptyList(),

    // 同步字段（必须和服务端对应）
    @SerializedName("is_deleted")
    val isDeleted: Boolean = false,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    constructor(
        order: Order
    ) : this(
        orderId = order.orderId,
        mealType = order.mealType,
        mealTime = order.mealTime,
        elderIds = order.elderIds,
        dishIds = order.dishIds,
        isDeleted = order.isDeleted,
        updatedAt = order.updatedAt,
        createdAt = order.createdAt
    )
}

data class OrderListDto(
    @SerializedName("order_list")
    val orderList: List<OrderDto> = emptyList()
)

data class AiChefRequest (
    @SerializedName("elder_count")
    val elderCount: Int,
    @SerializedName("meal_type")
    val mealType: String,
    @SerializedName("elder_info")
    val elderInfo: String,
    @SerializedName("order_hist")
    val orderHist: String,
    @SerializedName("selected_dishes")
    val selectedDishes: String,
    @SerializedName("user_prompt")
    val userPrompt: String,
    @SerializedName("refresh_count")
    val refreshCount: Int
)
package com.mrdben.autodishes.ordergraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.ordergraphs.api.OrderDto
import com.mrdben.autodishes.utils.getCurrentUtcTime

@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey
    val orderId: String,
    val userId: Int = 0,
    val mealType: MealType,
    val mealTime: String = getCurrentUtcTime(),
    val elderIds: List<String> = emptyList(),
    val dishIds: List<String> = emptyList(),

    val isDeleted: Boolean = false, // 软删除标记
    val syncStatus: SyncStatus = SyncStatus.TO_UPLOAD, // 同步状态
    val createdAt: String = getCurrentUtcTime(), // 创建时间， 服务器时间，非本地时间
    val updatedAt: String = getCurrentUtcTime() // 更新时间，服务器时间，非本地时间
){
    constructor(
        userId: Int,
        orderDto: OrderDto
    ) : this (
        orderId = orderDto.orderId,
        userId = userId,
        mealType = orderDto.mealType,
        mealTime = orderDto.mealTime,
        elderIds = orderDto.elderIds,
        dishIds = orderDto.dishIds,
        isDeleted = orderDto.isDeleted,
        syncStatus = SyncStatus.SYNCED,
        createdAt = orderDto.createdAt,
        updatedAt = orderDto.updatedAt
    )
}

// 订单列表类型别名（简化代码，更易读）
typealias OrderList = List<Order>

// 餐时段枚举：早饭/中午/晚饭（强类型，比字符串更安全）
enum class MealType {
    BREAKFAST,  // 早饭
    LUNCH,      // 午饭
    DINNER,      // 晚饭
    SNACK,       // 零食
    UNKNOWN
}


fun MealType.getDisplayName(): String {
    return when (this) {
        MealType.BREAKFAST -> "早餐"
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
        MealType.SNACK -> "零食"
        MealType.UNKNOWN -> "未知"
    }
}

val DEFAULT_ORDER = Order(
    orderId = "DEFAULT_ORDER",
    mealType = MealType.UNKNOWN
)
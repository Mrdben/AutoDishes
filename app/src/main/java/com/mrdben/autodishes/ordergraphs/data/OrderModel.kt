package com.mrdben.autodishes.ordergraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 订单数据结构
 * 纯数据模型，无任何Compose/UI相关代码
 * @param createTime 订单创建时间
 * @param mealType 餐时段（早/中/晚）
 * @param elderIds 老人ID列表
 * @param dishIds 餐品ID列表
 */
@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId : String,
    // 订单创建时间（推荐使用LocalDateTime，自带时间格式/比较）
    val createTime: Long = System.currentTimeMillis(),
    // 餐时段：早饭/中午/晚饭
    val mealType: MealType,
    // 老人ID列表（String类型）
    val elderIds: List<String>,
    // 餐品ID列表（String类型）
    val dishIds: List<String>
){
    // 转为用户时区的日期字符串（只显示年月日）
    fun formatDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(createTime))
    }
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
    orderId = "",
    createTime = 1744008000000,
    mealType = MealType.UNKNOWN,
    elderIds = emptyList(),
    dishIds = emptyList()
)
package com.mrdben.autodishes.familygraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 核心：家庭数据结构
@Entity(tableName = "family")
data class Family(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyId: String, // 家庭唯一标识（便于多设备同步/持久化）
    val familyName: String, // 家庭名称（比如“幸福之家”）
    val elderIds: List<String> = emptyList()
)

@Entity(tableName = "elder")
data class Elder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val elderId: String, // 老人唯一标识（便于增删改查）
    val name: String, // 姓名
    val gender: Gender, // 性别（枚举，避免魔法值）
    val age: Int, // 年龄
    val allergens: List<String> = emptyList(), // 过敏源（比如["花生","海鲜"]）
    val diseases: List<String> = emptyList(), // 疾病（比如["高血压","糖尿病"]）
    val dietaryRestrictions: List<String> = emptyList(), // 忌口（比如["辛辣","生冷"]）
    val tastePreferences: List<String> = emptyList() // 口味偏好（比如["清淡","偏甜"]）
)
typealias ElderList = List<Elder>

enum class Gender {
    MALE, FEMALE, UNKNOWN // 男/女/未知
}

// 可选：给枚举加友好展示名（便于UI显示）
fun Gender.getDisplayName(): String {
    return when (this) {
        Gender.MALE -> "男"
        Gender.FEMALE -> "女"
        Gender.UNKNOWN -> "未知"
    }
}
val DEFAULT_ELDER = Elder(
    elderId = "",
    name = "神秘老人",
    age = 0,
    gender = Gender.UNKNOWN
)
val DEFAULT_FAMILY : Family = Family (
    familyId = "family_debug_001",
    familyName = "我的家庭",
    elderIds = emptyList()
)

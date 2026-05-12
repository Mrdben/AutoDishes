package com.mrdben.autodishes.familygraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.ElderDto
import com.mrdben.autodishes.utils.getCurrentUtcTime

@Entity(tableName = "elder")
data class Elder(
    @PrimaryKey
    val elderId: String,
    val userId: Int = 0,
    val name: String, // 姓名
    val gender: Gender, // 性别（枚举，避免魔法值）
    val age: Int, // 年龄
    val allergens: List<String> = emptyList(), // 过敏源（比如["花生","海鲜"]）
    val diseases: List<String> = emptyList(), // 疾病（比如["高血压","糖尿病"]）
    val dietaryRestrictions: List<String> = emptyList(), // 忌口（比如["辛辣","生冷"]）
    val tastePreferences: List<String> = emptyList(), // 口味偏好（比如["清淡","偏甜"]）

    val isDeleted: Boolean = false, // 软删除标记
    val syncStatus: SyncStatus = SyncStatus.TO_UPLOAD, // 同步状态
    val createdAt: String = getCurrentUtcTime(), // 创建时间， 服务器时间，非本地时间
    val updatedAt: String = getCurrentUtcTime() // 更新时间，服务器时间，非本地时间
) {
    constructor(
        userId : Int,
        elderDto: ElderDto
    ) : this (
        elderId = elderDto.elderId,
        userId = userId,
        name = elderDto.name,
        gender = elderDto.gender,
        age = elderDto.age,
        allergens = elderDto.allergens,
        diseases = elderDto.diseases,
        dietaryRestrictions = elderDto.dietaryRestrictions,
        tastePreferences = elderDto.tastePreferences,
        isDeleted = elderDto.isDeleted,
        syncStatus = SyncStatus.SYNCED,
        updatedAt = elderDto.updatedAt,
        createdAt = elderDto.createdAt
    )
}

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
    name = "神秘老人",
    elderId = "DEFAULT_MEMBER",
    age = 0,
    gender = Gender.UNKNOWN
)

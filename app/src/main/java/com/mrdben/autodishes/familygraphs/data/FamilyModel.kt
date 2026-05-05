package com.mrdben.autodishes.familygraphs.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrdben.autodishes.data.SyncStatus
import com.mrdben.autodishes.familygraphs.api.FamilyDto
import com.mrdben.autodishes.utils.getCurrentUtcTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// 核心：家庭数据结构
@Entity(tableName = "family")
data class Family(
    @PrimaryKey
    val familyId: Int = 0, // managed by server
    val userId: Int = 0, // owner of this family
    val familyName: String, // 家庭名称（比如“幸福之家”）
    val elderIds: List<String> = emptyList(),

    val isDeleted: Boolean = false, // 软删除标记
    val syncStatus: SyncStatus = SyncStatus.TO_UPLOAD, // 同步状态
    val createdAt: String = getCurrentUtcTime(), // 创建时间， 服务器时间，非本地时间
    val updatedAt: String = getCurrentUtcTime() // 更新时间，服务器时间，非本地时间
) {
    constructor(
        familyDto: FamilyDto
    ) : this (
        familyId = familyDto.familyId,
        userId = familyDto.userId,
        familyName = familyDto.familyName,
        elderIds = familyDto.elderIds,
        isDeleted = familyDto.isDeleted,
        updatedAt = familyDto.updatedAt,
        createdAt = familyDto.createdAt,
        syncStatus = SyncStatus.SYNCED
    )
}
val DEFAULT_FAMILY : Family = Family (
    familyName = "我的默认家庭",
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
    val tastePreferences: List<String> = emptyList(), // 口味偏好（比如["清淡","偏甜"]）

    val serverId: String? = null, // 服务端生成的唯一ID，未上传 = null
    val isDeleted: Boolean = false, // 软删除标记
    val syncStatus: SyncStatus = SyncStatus.TO_UPLOAD, // 同步状态
    val createdAt: String = getCurrentUtcTime(), // 创建时间， 服务器时间，非本地时间
    val updatedAt: String = getCurrentUtcTime() // 更新时间，服务器时间，非本地时间
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

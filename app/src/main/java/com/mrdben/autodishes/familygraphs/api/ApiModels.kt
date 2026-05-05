package com.mrdben.autodishes.familygraphs.api

import com.google.gson.annotations.SerializedName
import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.Family
import com.mrdben.autodishes.familygraphs.data.Gender


// 这是 与后端 FastAPI 通信的模型（和 Room 无关）
data class FamilyDto(
    @SerializedName("family_id")
    val familyId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("family_name")
    val familyName: String,

    // 同步字段（必须和服务端对应）
    @SerializedName("is_deleted")
    val isDeleted: Boolean = false,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    constructor(
        family: Family
    ) : this (
        familyId = family.familyId,
        userId = family.userId,
        familyName = family.familyName,
        isDeleted = family.isDeleted,
        updatedAt = family.updatedAt,
        createdAt = family.createdAt
    )
}

data class UpdateResponse(
    @SerializedName("item_id")
    val itemId: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class PullElderRequest(
    @SerializedName("server_id")
    val serverId: Int
)
data class ElderDto(
    @SerializedName("server_id")
    val serverId: Int,
    @SerializedName("elder_id")
    val elderId: String,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    val gender: Gender,
    var age: Int,
    val allergens: List<String> = emptyList(),
    val diseases: List<String> = emptyList(),
    @SerializedName("dietary_restriction")
    val dietaryRestrictions: List<String> = emptyList(),
    @SerializedName("taste_preferences")
    val tastePreferences: List<String> = emptyList(),

    // 同步字段（必须和服务端对应）
    @SerializedName("is_deleted")
    val isDeleted: Boolean = false,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("created_at")
    val createdAt: String
    ){
    constructor(
        elder : Elder
    ) : this(
        serverId = elder.serverId,
        elderId = elder.elderId,
        userId = elder.userId,
        name = elder.name,
        gender = elder.gender,
        age = elder.age,
        allergens = elder.allergens,
        diseases = elder.diseases,
        dietaryRestrictions = elder.dietaryRestrictions,
        tastePreferences = elder.tastePreferences,
        isDeleted = elder.isDeleted,
        updatedAt = elder.updatedAt,
        createdAt = elder.createdAt
    )
}
data class ElderListDto(
    @SerializedName("elder_list")
    val elderList: List<ElderDto> = emptyList()
)
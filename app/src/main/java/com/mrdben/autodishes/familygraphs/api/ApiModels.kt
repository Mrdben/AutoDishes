package com.mrdben.autodishes.familygraphs.api

import com.google.gson.annotations.SerializedName
import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.Gender

data class UpdateInfo(
    @SerializedName("elder_id")
    val elderId: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UpdateInfoList(
    @SerializedName("info_list")
    val infoList: List<UpdateInfo> = emptyList()
)

data class ElderDto(
    @SerializedName("elder_id")
    val elderId: String,
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
        elderId = elder.elderId,
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
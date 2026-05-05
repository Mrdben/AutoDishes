package com.mrdben.autodishes.familygraphs.api

import com.google.gson.annotations.SerializedName
import com.mrdben.autodishes.familygraphs.data.Family


// 这是 与后端 FastAPI 通信的模型（和 Room 无关）
data class FamilyDto(
    @SerializedName("family_id")
    val familyId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("family_name")
    val familyName: String,
    @SerializedName("elder_ids")
    val elderIds: List<String> = emptyList(),

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
        elderIds = family.elderIds,
        isDeleted = family.isDeleted,
        updatedAt = family.updatedAt,
        createdAt = family.createdAt
    )
}

data class FamilyUpdateResponse(
    @SerializedName("family_id")
    val familyId: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)
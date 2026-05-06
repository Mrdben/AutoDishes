package com.mrdben.autodishes.familygraphs.api

import retrofit2.http.*

interface FamilyApiService {

    // 1. 上传当前家庭到服务端
    @POST("families/push")
    suspend fun pushFamilyToServer(
        @Body request: FamilyDto
    ): UpdateResponse

    // 2. 从服务端拉取当前用户的家庭
    @GET("families/pull")
    suspend fun getFamilyFromServer(): FamilyDto
}

interface ElderApiService {
    // push = add, update, soft_delete must one by one
    // because need to write (id, updatedAt) back to local one by one
    @POST("elders/push")
    suspend fun pushElderToServer(
        @Body request: ElderDto
    ): UpdateResponse
    @POST("elders/pull")
    suspend fun pullElderFromServer(
        @Body request: PullElderRequest
    ): ElderDto
    @GET("elders/batch_pull")
    suspend fun getEldersFromServer(): ElderListDto
}
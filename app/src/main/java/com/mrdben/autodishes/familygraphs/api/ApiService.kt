package com.mrdben.autodishes.familygraphs.api

import retrofit2.http.*

interface FamilyApiService {

    // 1. 上传当前家庭到服务端
    @POST("families/push")
    suspend fun pushFamilyToServer(
        @Body request: FamilyDto
    ): FamilyUpdateResponse

    // 2. 从服务端拉取当前用户的家庭
    @GET("families/pull")
    suspend fun getFamilyFromServer(): FamilyDto
}
package com.mrdben.autodishes.familygraphs.api

import retrofit2.http.*

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
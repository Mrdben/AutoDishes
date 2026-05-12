package com.mrdben.autodishes.familygraphs.api

import retrofit2.http.*

interface ElderApiService {
    @POST("elders/push")
    suspend fun pushEldersToServer(
        @Body request: ElderListDto
    ): UpdateInfoList

    @POST("elders/pull")
    suspend fun pullEldersFromServer(
        @Body request: UpdateInfoList
    ): ElderListDto

}
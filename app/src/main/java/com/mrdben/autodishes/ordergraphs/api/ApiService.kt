package com.mrdben.autodishes.ordergraphs.api

import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApiService {
    @POST("orders/push")
    suspend fun pushOrdersToServer(
        @Body request: OrderListDto
    ): UpdateInfoList

    @POST("orders/pull")
    suspend fun pullOrdersFromServer(
        @Body request: UpdateInfoList
    ): OrderListDto

}
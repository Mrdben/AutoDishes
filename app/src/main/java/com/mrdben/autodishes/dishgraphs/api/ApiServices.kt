package com.mrdben.autodishes.dishgraphs.api

import retrofit2.http.*

interface DishApiService {
    @POST("dishes/push")
    suspend fun pushDishesToServer(
        @Body request: DishListDto
    ): UpdateInfoList

    @POST("dishes/pull")
    suspend fun pullDishesFromServer(
        @Body request: UpdateInfoList
    ): DishListDto

}
package com.mrdben.autodishes.authgraphs.api

import retrofit2.http.*

interface ApiService {

    // 1. 用户注册
    // 使用 @Body 直接传递整个数据对象
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    // 2. 用户登录
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // 3. 刷新 Token
    @POST("refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse

    // 4. 获取当前用户信息 (需要携带 Token)
    @GET("users/me")
    suspend fun getUserInfo(): UserInfoResponse

    // 5. 修改密码
    @POST("users/me/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    )

    // 6. 修改手机号
    @POST("users/me/update-phone-number")
    suspend fun updatePhoneNumber(
        @Body request: UpdatePhoneNumberRequest
    )

    // 7. 注销用户
    @DELETE("users/me")
    suspend fun deleteAccount(
        @Body request: DeleteUserRequest
    )
}
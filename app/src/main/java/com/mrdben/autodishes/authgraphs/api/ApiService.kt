package com.mrdben.autodishes.authgraphs.api

import retrofit2.http.*

interface AuthApiService {

    // 1. 用户注册
    // 使用 @Body 直接传递整个数据对象
    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    // 2. 用户登录
    @POST("users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // 3. 刷新 Token
    @POST("users/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponse

    // 4. 获取当前用户信息 (需要携带 Token)
    @GET("users/me")
    suspend fun getUserInfo(): UserInfoResponse

    // 5. 修改密码
    @POST("users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    )

    // 6. 修改手机号
    @POST("users/update-phone-number")
    suspend fun updatePhoneNumber(
        @Body request: UpdatePhoneNumberRequest
    )

    // 7. 注销用户
    @POST("users/delete-account")
    suspend fun deleteAccount(
        @Body request: DeleteUserRequest
    )

    @POST("users/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    )
}
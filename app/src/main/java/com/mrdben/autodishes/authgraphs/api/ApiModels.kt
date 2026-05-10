package com.mrdben.autodishes.authgraphs.api

import com.google.gson.annotations.SerializedName

// ================= 注册相关 =================

// 对应后端 UserRegisterRequest
data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("family_name")
    val familyName: String
)

// 对应后端 UserRegisterResponse
data class RegisterResponse(
    val message: String,
    val username: String
)

// ================= 登录相关 =================

// 对应后端 UserLoginRequest
data class LoginRequest(
    val username: String,
    val password: String
)

// 对应后端 UserLoginResponse
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

// ================= 刷新 Token 相关 =================

// 对应后端 RefreshTokenRequest
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

// 对应后端 RefreshTokenResponse
data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

// ================= 用户信息与操作相关 =================

// 对应后端 GetUserByTokenResponse
data class UserInfoResponse(
    val id: Int,
    val username: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("family_name")
    val familyName: String
)

// 对应后端 ChangePasswordRequest
data class ChangePasswordRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("new_password")
    val newPassword: String
)

// 对应后端 UpdatePhoneNumberRequest
data class UpdatePhoneNumberRequest(
    val password: String,
    @SerializedName("old_phone_number")
    val oldPhoneNumber: String,
    @SerializedName("new_phone_number")
    val newPhoneNumber: String
)

data class UpdateFamilyNameRequest(
    @SerializedName("family_name")
    val familyName: String
)

// 对应后端 DeleteUserRequest (虽然 DELETE 接口通常不带 Body，但这是对应的数据结构)
data class DeleteUserRequest(
    val password: String,
    @SerializedName("phone_number")
    val phoneNumber: String
)

data class ResetPasswordRequest(
    val username: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("new_password")
    val newPassword: String
)
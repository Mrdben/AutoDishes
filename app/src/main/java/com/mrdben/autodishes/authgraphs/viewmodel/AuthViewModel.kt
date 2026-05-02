package com.mrdben.autodishes.authgraphs.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.mrdben.autodishes.api.ResultWrapper
import com.mrdben.autodishes.api.RetrofitClient
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.authgraphs.api.*

sealed class AuthState {
    object Idle : AuthState() // 初始状态
    object Loading : AuthState() // 登录中
    data class Success(val message: String) : AuthState() // 成功
    data class Error(val message: String) : AuthState() // 失败
}
data class UserProfile (
    val username: String = "",
    val phoneNumber: String = "",
    val registryTime: String = ""
)
class AuthViewModel : ViewModel()  {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    fun setAuthState(newState : AuthState) {
        _authState.value = newState
    }

    private val _userProfile  = MutableStateFlow<UserProfile>(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            val (username, phoneNumber, registryTime) = TokenManager.getLoginUser()

            // 赋值给你的 StateFlow
            _userProfile.value = UserProfile(
                username = username,
                phoneNumber = phoneNumber,
                registryTime = registryTime
            )
        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            //_authState.value = AuthState.Loading
            val response = safeApiCall{ RetrofitClient.apiService.getUserInfo() }
            when (response) {
                is ResultWrapper.Success -> {
                    val username = response.data.username
                    val phoneNumber = response.data.phoneNumber
                    val registryTime = response.data.createdAt
                    _userProfile.value = UserProfile(username = username,
                                                     phoneNumber = phoneNumber,
                                                     registryTime = registryTime)
                    TokenManager.saveLoginUser(username, phoneNumber, registryTime)
                    Log.d("AuthViewModel", "getUserInfo OK: username = ${response.data.username}, phoneNumber=${response.data.phoneNumber}, creationTime=${response.data.createdAt}")
                }
                is ResultWrapper.Error -> {
                    _userProfile.value = UserProfile()
                    Log.d("AuthViewModel", "getUserInfo FAILED")
                }
            }
            //_authState.value = AuthState.Idle
        }
    }
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = LoginRequest(username, password)
            val response = safeApiCall{ RetrofitClient.apiService.login(request) }

            when (response) {
                is ResultWrapper.Success -> {
                    val accessToken = response.data.accessToken
                    val refreshToken = response.data.refreshToken
                    TokenManager.saveAccessToken(accessToken)
                    TokenManager.saveRefreshToken(refreshToken)
                    Log.d("AuthViewModel", "✅ 登录成功！获取到的 accessToken: $accessToken")
                    Log.d("AuthViewModel", "✅ 登录成功！获取到的 refreshToken: $refreshToken")

                    getUserProfile() //always refresh user info after login
                    _authState.value = AuthState.Success("登录成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 登录失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }

    fun register(username: String, password: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = RegisterRequest(username, password, phoneNumber)
            val response = safeApiCall{ RetrofitClient.apiService.register(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val usernameReturn = response.data.username
                    Log.d("AuthViewModel", "✅ 用户注册成功！返回的用户名: $usernameReturn")
                    _authState.value = AuthState.Success("用户注册成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 用户注册失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }
    fun changePassword(oldPassword: String, newPassword: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = ChangePasswordRequest(oldPassword = oldPassword,
                                                phoneNumber = phoneNumber,
                                                newPassword = newPassword)
            val response = safeApiCall{ RetrofitClient.apiService.changePassword(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("AuthViewModel", "✅ 修改密码成功！ $responseMsg")
                    _authState.value = AuthState.Success("修改密码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 修改密码失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }
    fun updatePhoneNumber(password: String, newPhoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = UpdatePhoneNumberRequest(password = password,
                                                   oldPhoneNumber = _userProfile.value.phoneNumber,
                                                   newPhoneNumber = newPhoneNumber)
            val response = safeApiCall{ RetrofitClient.apiService.updatePhoneNumber(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 更新手机号码成功！ $responseMsg")
                    _authState.value = AuthState.Success("更新手机号码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 更新手机号码失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            TokenManager.clearAll()
            _authState.value = AuthState.Success("已退出登录")
        }
    }
    fun deleteAccount(password: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = DeleteUserRequest(password = password,
                                            phoneNumber = phoneNumber)
            val response = safeApiCall{ RetrofitClient.apiService.deleteAccount(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 账户移除成功！ $responseMsg")
                    _authState.value = AuthState.Success("账户移除成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 账户移除失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }

    fun resetPassword(username: String, phoneNumber: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = ResetPasswordRequest(username = username,
                                               phoneNumber = phoneNumber,
                                               newPassword = newPassword)
            val response = safeApiCall{ RetrofitClient.apiService.resetPassword(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 重置密码成功！ $responseMsg")
                    _authState.value = AuthState.Success("重置密码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 重置密码失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }
}
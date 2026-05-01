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

class AuthViewModel : ViewModel()  {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginUser = MutableStateFlow("")
    val loginUser: StateFlow<String> = _loginUser.asStateFlow()

    init {
        viewModelScope.launch {
            _loginUser.value = TokenManager.getLoginUser()?:""
        }
    }
    fun setAuthState(newState : AuthState) {
        _authState.value = newState
    }
    // 检查是否已登录（根据是否有 Token 判断） 不能只判断是否有accessToken，
    // accesstoken在内存，如果没有需要用refreshtoken刷新
    // 判断是否已经登录的方法，是调用一次getUserInfo, 看是否能拿到用户信息
    // TODO: 修改判断逻辑
    fun checkLoginStatus() {
        viewModelScope.launch {
            val token = TokenManager.getAccessToken()
            if (!token.isNullOrEmpty()) {
                // 有 Token，视为已登录（或者可以在这里主动调一下 UserInfo 接口验证 Token 有效性）
                _authState.value = AuthState.Success("已自动登录")
            } else {
                _authState.value = AuthState.Error("未登录")
            }
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
                    TokenManager.saveLoginUser( username)
                    Log.d("NetworkTest", "✅ 登录成功！获取到的 accessToken: $accessToken")
                    Log.d("NetworkTest", "✅ 登录成功！获取到的 refreshToken: $refreshToken")
                    _authState.value = AuthState.Success("登录成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 登录失败！错误码: $code, 错误信息: $message")
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
                    Log.d("NetworkTest", "✅ 用户注册成功！返回的用户名: $usernameReturn")
                    _authState.value = AuthState.Success("用户注册成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 用户注册失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error(message)
                }
            }
        }
    }
    fun changePassword(oldPassword: String, newPassword: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = ChangePasswordRequest(oldPassword, newPassword, phoneNumber)
            val response = safeApiCall{ RetrofitClient.apiService.changePassword(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 修改密码成功！ $responseMsg")
                    _authState.value = AuthState.Success("修改密码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 修改密码失败！错误码: $code, 错误信息: $message")
                    _authState.value = AuthState.Error("修改密码失败！")
                }
            }
        }
    }
    fun updatePhoneNumber(password: String, oldPhoneNumber: String, newPhoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = UpdatePhoneNumberRequest(password, oldPhoneNumber, newPhoneNumber)
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
                    _authState.value = AuthState.Error("更新手机号码失败！")
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
    fun deleteAccount(username: String, password: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val request = DeleteUserRequest(password, phoneNumber)
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
                    _authState.value = AuthState.Error("账户移除失败！")
                }
            }
        }
    }
}
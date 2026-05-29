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
import com.mrdben.autodishes.api.UserProfile
import com.mrdben.autodishes.api.safeApiCall
import com.mrdben.autodishes.authgraphs.api.*
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.familygraphs.data.ElderList
import com.mrdben.autodishes.familygraphs.data.FamilyRepository

class AuthViewModel (
    private val repository : FamilyRepository
): ViewModel()  {

    private val _authState = MutableStateFlow<WorkState>(WorkState.Idle)
    val authState: StateFlow<WorkState> = _authState.asStateFlow()
    fun setWorkState(newState : WorkState) {
        _authState.value = newState
    }

    val userProfile: StateFlow<UserProfile> = TokenManager.userProfile
    val elders: StateFlow<ElderList> = repository.elders

    suspend fun getUserProfileFromServer() {
        //_authState.value = WorkState.Loading
        val response = safeApiCall{ RetrofitClient.authApiService.getUserInfo() }
        when (response) {
            is ResultWrapper.Success -> {
                val userProfile = UserProfile(userId =  response.data.id,
                                              username = response.data.username,
                                              familyName = response.data.familyName,
                                              phoneNumber = response.data.phoneNumber,
                                              registryTime = response.data.createdAt)
                Log.d("AuthViewModel", "getUserInfo OK: username = ${userProfile.username}, phoneNumber=${userProfile.phoneNumber}, creationTime=${userProfile.registryTime}")
                TokenManager.saveLoginUser(userProfile)
            }
            is ResultWrapper.Error -> {
                // _userProfile.value = UserProfile()
                Log.e("AuthViewModel", "getUserInfo FAILED")
            }
        }
        //_authState.value = WorkState.Idle
    }
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading

            val request = LoginRequest(username, password)
            val response = safeApiCall{ RetrofitClient.authApiService.login(request) }

            when (response) {
                is ResultWrapper.Success -> {
                    val accessToken = response.data.accessToken
                    val refreshToken = response.data.refreshToken
                    TokenManager.saveAccessToken(accessToken)
                    TokenManager.saveRefreshToken(refreshToken)
                    Log.d("AuthViewModel", "✅ 登录成功！获取到的 accessToken: $accessToken")
                    Log.d("AuthViewModel", "✅ 登录成功！获取到的 refreshToken: $refreshToken")

                    getUserProfileFromServer() //always refresh user info after login, and save user info to tokenManager
                    _authState.value = WorkState.Success("登录成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 登录失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }

    fun register(username: String,
                 password: String,
                 phoneNumber: String,
                 verifyCode: String,
                 familyName: String = "我的默认家庭") {
        viewModelScope.launch {
            _authState.value = WorkState.Loading
            val request = RegisterRequest(username, password, phoneNumber, verifyCode, familyName)
            val response = safeApiCall{ RetrofitClient.authApiService.register(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val usernameReturn = response.data.username
                    Log.d("AuthViewModel", "✅ 用户注册成功！返回的用户名: $usernameReturn")
                    TokenManager.saveLoginUser(UserProfile(
                        userId = 0,
                        username = username,
                        familyName = familyName,
                        phoneNumber = phoneNumber,
                        registryTime = "")
                    )
                    _authState.value = WorkState.Success("用户注册成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 用户注册失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }
    fun changePassword(oldPassword: String,
                       newPassword: String,
                       phoneNumber: String,
                       verifyCode: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading

            val request = ChangePasswordRequest(oldPassword = oldPassword,
                                                phoneNumber = phoneNumber,
                                                newPassword = newPassword,
                                                validationCode = verifyCode)
            val response = safeApiCall{ RetrofitClient.authApiService.changePassword(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("AuthViewModel", "✅ 修改密码成功！ $responseMsg")
                    _authState.value = WorkState.Success("修改密码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("AuthViewModel", "❌ 修改密码失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }
    fun updatePhoneNumber(password: String,
                          newPhoneNumber: String,
                          verifyCode: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading
            val request = UpdatePhoneNumberRequest(password = password,
                                                   oldPhoneNumber = userProfile.value.phoneNumber,
                                                   newPhoneNumber = newPhoneNumber,
                                                   validationCode = verifyCode)
            val response = safeApiCall{ RetrofitClient.authApiService.updatePhoneNumber(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 更新手机号码成功！ $responseMsg")
                    TokenManager.updatePhoneNumber(newPhoneNumber)
                    _authState.value = WorkState.Success("更新手机号码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 更新手机号码失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }
    fun updateFamilyName(newFamilyName: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading
            val request = UpdateFamilyNameRequest(familyName = newFamilyName)
            val response = safeApiCall{ RetrofitClient.authApiService.updateFamilyName(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 更新家庭名称成功！ $responseMsg")
                    TokenManager.updateFamilyName(newFamilyName)
                    _authState.value = WorkState.Success("更新家庭名称成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 更新家庭名称失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            TokenManager.clearAll()
            _authState.value = WorkState.Success("已退出登录")
        }
    }
    fun deleteAccount(password: String, phoneNumber: String, verifyCode: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading

            val request = DeleteUserRequest(password = password,
                                            phoneNumber = phoneNumber,
                                            validationCode = verifyCode)
            val response = safeApiCall{ RetrofitClient.authApiService.deleteAccount(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 账户移除成功！ $responseMsg")
                    _authState.value = WorkState.Success("账户移除成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 账户移除失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }

    fun resetPassword(username: String,
                      phoneNumber: String,
                      newPassword: String,
                      verifyCode: String) {
        viewModelScope.launch {
            _authState.value = WorkState.Loading

            val request = ResetPasswordRequest(username = username,
                                               phoneNumber = phoneNumber,
                                               newPassword = newPassword,
                                               validationCode = verifyCode)
            val response = safeApiCall{ RetrofitClient.authApiService.resetPassword(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    Log.d("NetworkTest", "✅ 重置密码成功！ $responseMsg")
                    _authState.value = WorkState.Success("重置密码成功！")
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    Log.e("NetworkTest", "❌ 重置密码失败！错误码: $code, 错误信息: $message")
                    _authState.value = WorkState.Error(message)
                }
            }
        }
    }

    fun validationCodeRequest(phoneNumber: String, scene: String) {
        viewModelScope.launch {
            val request = ValidationSMSRequest(phoneNumber = phoneNumber, scene = scene)
            val response = safeApiCall{ RetrofitClient.authApiService.requestValidationCode(request) }
            when (response) {
                is ResultWrapper.Success -> {
                    val responseMsg = response.data
                    if (responseMsg.success) {
                        _authState.value = WorkState.Info("【速通互联验证码】已发送！")
                        Log.d("NetworkTest", "✅ 验证码请求成功！ ${responseMsg.message}")
                    }
                    else {
                        _authState.value = WorkState.Info("验证码发送失败！\n${responseMsg.message}")
                        Log.e("NetworkTest", "❌ 验证码请求失败！ ${responseMsg.message}")
                    }
                }
                is ResultWrapper.Error -> {
                    val code = response.apiError.statusCode
                    val message = response.apiError.message
                    _authState.value = WorkState.Info("验证码发送失败！\n错误码: $code\n$message")
                    Log.e("NetworkTest", "❌ 验证码网络请求失败！错误码: $code, 错误信息: $message")
                }
            }
        }
    }
}
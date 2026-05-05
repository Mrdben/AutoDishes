package com.mrdben.autodishes.api

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// 1. 初始化 DataStore (仅用于持久化存储 Refresh Token)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

data class UserProfile (
    val userid: Int = 0,
    val username: String = "",
    val phoneNumber: String = "",
    val registryTime: String = ""
)
object TokenManager {
    private lateinit var appContext: Context
    var hasRefreshToken : Boolean = true
    var loginUserId : Int = 0
    fun init(context: Context) {
        // 强制使用 ApplicationContext，这是单例持有 Context 的最佳实践
        appContext = context.applicationContext
        hasRefreshToken = runBlocking { !getRefreshToken().isNullOrBlank() }
        loginUserId = runBlocking { getLoginUserId() }
    }

    private val LOGIN_USER_ID_KEY = intPreferencesKey("login_user_id")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val LOGIN_USERNAME_KEY = stringPreferencesKey("login_username")
    private val LOGIN_PHONE_NUMBER_KEY = stringPreferencesKey("login_phone_number")
    private val LOGIN_REGISTRY_TIME  = stringPreferencesKey("login_registry_time")
    private var inMemoryAccessToken: String? = null
    // --- Access Token 管理 (内存) ---
    // 保存 Access Token 到内存
    fun saveAccessToken(accessToken: String) {
        Log.d("TokenManager", "saveAccessToken $accessToken")
        inMemoryAccessToken = accessToken
    }

    // 从内存获取 Access Token
    fun getAccessToken(): String? {
        Log.d("TokenManager", "getAccessToken $inMemoryAccessToken")
        return inMemoryAccessToken
    }

    // 清除内存中的 Access Token
    fun clearAccessToken() {
        Log.d("TokenManager", "clear AccessToken")
        inMemoryAccessToken = null
    }

    // --- Refresh Token 管理 (本地持久化) ---

    // 保存 Refresh Token 到本地 DataStore
    suspend fun saveRefreshToken(refreshToken: String) {
        Log.d("TokenManager", "saveRefreshToken $refreshToken")
        appContext.dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
        hasRefreshToken = true
    }

    // 从本地 DataStore 获取 Refresh Token
    suspend fun getRefreshToken(): String? {
        val token = appContext.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }.first()
        Log.d("TokenManager", "getRefreshToken: $token")
        return token
    }

    suspend fun saveLoginUser(userProfile: UserProfile) {
        Log.d("TokenManager", "save user ${userProfile.username}")
        loginUserId = userProfile.userid
        appContext.dataStore.edit { prefs ->
            prefs[LOGIN_USER_ID_KEY] = userProfile.userid
            prefs[LOGIN_USERNAME_KEY] = userProfile.username
            prefs[LOGIN_PHONE_NUMBER_KEY] = userProfile.phoneNumber
            prefs[LOGIN_REGISTRY_TIME] = userProfile.registryTime
        }
    }
    suspend fun getLoginUser(): UserProfile {
        val prefs = appContext.dataStore.data.first()
        val userProfile = UserProfile(
            userid = prefs[LOGIN_USER_ID_KEY] ?: 0,
            username = prefs[LOGIN_USERNAME_KEY] ?: "",
            phoneNumber = prefs[LOGIN_PHONE_NUMBER_KEY] ?: "",
            registryTime = prefs[LOGIN_REGISTRY_TIME] ?: ""
        )
        Log.d("TokenManager", "load user ${userProfile.username}")
        // 直接返回 3 个字符串
        return userProfile
    }
    suspend fun getLoginUserId(): Int {
        val prefs = appContext.dataStore.data.first()
        val userId = prefs[LOGIN_USER_ID_KEY] ?: 0
        Log.d("TokenManager", "load user id $userId")
        // 直接返回 3 个字符串
        return userId
    }
    // --- 登出 / 清除所有凭证 ---
    suspend fun clearAll() {
        Log.d("TokenManager", ">>>>>> clear all !!!!!!!")
        // 1. 清除内存中的 Access Token
        clearAccessToken()
        hasRefreshToken = false
        loginUserId = 0
        // 2. 清除本地持久化的 Refresh Token
        appContext.dataStore.edit { prefs ->
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(LOGIN_PHONE_NUMBER_KEY)
            prefs.remove(LOGIN_REGISTRY_TIME)
        }
    }
}
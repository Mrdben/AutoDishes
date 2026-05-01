package com.mrdben.autodishes.api

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 1. 初始化 DataStore (仅用于持久化存储 Refresh Token)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

object TokenManager {
    private lateinit var appContext: Context
    fun init(context: Context) {
        // 强制使用 ApplicationContext，这是单例持有 Context 的最佳实践
        appContext = context.applicationContext
    }

    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val LAST_LOGIN_USER_KEY = stringPreferencesKey("last_user")

    // 内存中保存短期的 Access Token
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
        inMemoryAccessToken = null
    }

    // --- Refresh Token 管理 (本地持久化) ---

    // 保存 Refresh Token 到本地 DataStore
    suspend fun saveRefreshToken(refreshToken: String) {
        Log.d("TokenManager", "saveRefreshToken $refreshToken")
        appContext.dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // 从本地 DataStore 获取 Refresh Token
    suspend fun getRefreshToken(): String? {
        val token = appContext.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }.first()
        Log.d("TokenManager", "getRefreshToken: $token")
        return token
    }

    suspend fun saveLoginUser(lastUser: String) {
        Log.d("TokenManger", "save user $lastUser")
        appContext.dataStore.edit { prefs ->
            prefs[LAST_LOGIN_USER_KEY] = lastUser
        }
    }
    suspend fun getLoginUser(): String? {
        val user = appContext.dataStore.data.map { prefs ->
            prefs[LAST_LOGIN_USER_KEY]
        }.first()
        Log.d("TokenManager", "load user $user")
        return user
    }
    // --- 登出 / 清除所有凭证 ---
    suspend fun clearAll() {
        Log.d("TokenManger", ">>>>>> clear all !!!!!!!")
        // 1. 清除内存中的 Access Token
        clearAccessToken()
        // 2. 清除本地持久化的 Refresh Token
        appContext.dataStore.edit { prefs ->
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }
}
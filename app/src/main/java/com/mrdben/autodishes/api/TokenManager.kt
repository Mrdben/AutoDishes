package com.mrdben.autodishes.api

import android.content.Context
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
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

    // 内存中保存短期的 Access Token
    private var inMemoryAccessToken: String? = null

    // --- Access Token 管理 (内存) ---

    // 保存 Access Token 到内存
    fun saveAccessToken(accessToken: String) {
        inMemoryAccessToken = accessToken
    }

    // 从内存获取 Access Token
    fun getAccessToken(): String? {
        return inMemoryAccessToken
    }

    // 清除内存中的 Access Token
    fun clearAccessToken() {
        inMemoryAccessToken = null
    }

    // --- Refresh Token 管理 (本地持久化) ---

    // 保存 Refresh Token 到本地 DataStore
    suspend fun saveRefreshToken(context: Context, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // 从本地 DataStore 获取 Refresh Token
    suspend fun getRefreshToken(context: Context): String? {
        return context.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }.first()
    }

    // --- 登出 / 清除所有凭证 ---

    suspend fun clearAll(context: Context) {
        // 1. 清除内存中的 Access Token
        clearAccessToken()
        // 2. 清除本地持久化的 Refresh Token
        context.dataStore.edit { prefs ->
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }
}
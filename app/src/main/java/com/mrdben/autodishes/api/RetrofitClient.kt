package com.mrdben.autodishes.api

import android.util.Log
import com.mrdben.autodishes.authgraphs.api.*
import com.mrdben.autodishes.familygraphs.api.*

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://39.106.169.94:8080/"

    private fun addTokenToRequest(originalRequest: okhttp3.Request, accessToken: String?) : okhttp3.Request {
        if (accessToken.isNullOrEmpty()) {
            return originalRequest
        }
        val originalHttpUrl = originalRequest.url
        val newUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("token", accessToken)
            .build()

        // 4. 构建新的请求，使用新的 URL
        val requestWithToken = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        return requestWithToken
    }
    private fun refreshAccessToken(refreshToken: String?) : String? {
        if (!refreshToken.isNullOrEmpty()) {
            val newAccessToken = try {
                val response = runBlocking {
                    refreshService.refreshToken(RefreshTokenRequest(refreshToken))
                }
                response.accessToken
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    if (e.code() == 401) {
                        runBlocking {
                            TokenManager.clearAll()
                        }
                    }
                }
                null
            }
            return newAccessToken
        }
        // refresh token failed. sign out
        runBlocking {
            TokenManager.clearAll()
        }
        return null
    }
    // 认证拦截器：负责给 URL 添加 token 查询参数
    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // 1. 获取当前存储的 Token
            val savedToken = TokenManager.getAccessToken()
            var accessToken = savedToken
            var refreshToken : String? = null //暂时不读取

            // 2. 如果没有refresh Token，说明还没有login成功，就直接放行原始请求（比如登录、注册等不需要鉴权的接口）
            // 如果每个请求，都从datastore读取一次refreshToken，会造成多次读取。
            // 这里做一个标记。真正读取只在需要refresh时。
            if (!TokenManager.hasRefreshToken) {
                return chain.proceed(originalRequest)
            }

            // 3. 确认已有refreshToken，执行请求
            repeat(2) {
                if (accessToken.isNullOrEmpty()) {
                    Log.d("Retrofit","accessToken is empty. refresh it with refreshToken.")
                    if (refreshToken.isNullOrBlank()) {
                        refreshToken = runBlocking { TokenManager.getRefreshToken() }
                    }
                    accessToken = refreshAccessToken(refreshToken)
                    if (accessToken.isNullOrEmpty()) { // cannot refresh access token with refreshToken, return 401 error
                        Log.d("Retrofit","failed to get accessToken from refreshToken. Logoff!")
                        return Response.Builder()
                            .request(originalRequest)
                            .protocol(Protocol.HTTP_1_1)
                            .code(401)
                            .message("Token Refresh Failed: Account Logged out")
                            .body("".toResponseBody(null))
                            .build()
                    }
                }
                val response = chain.proceed(addTokenToRequest(originalRequest, accessToken))
                if (response.code == 401) {
                    response.close() // 关闭旧响应
                    Log.d("Retrofit","bad accessToken ! try to refresh.")
                    accessToken = null // reset access token
                    TokenManager.clearAccessToken()
                } else {
                    if (accessToken != savedToken) {
                        TokenManager.saveAccessToken(accessToken)
                    }
                    return response
                }
            }

            return chain.proceed(originalRequest) //搞不定
        }
    }

    // 专门用于刷新 Token 的 Retrofit 实例（不带拦截器，防止死循环）
    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val refreshService = refreshRetrofit.create(AuthApiService::class.java)

    // 专门用于正常请求的 OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // 添加认证拦截器
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // 正常业务请求的 Retrofit 实例
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    val familyApiService: FamilyApiService by lazy {
        retrofit.create(FamilyApiService::class.java)
    }

}
package com.mrdben.autodishes.api

import android.util.Log
import com.mrdben.autodishes.authgraphs.api.AuthApiService
import com.mrdben.autodishes.authgraphs.api.RefreshTokenRequest
import com.mrdben.autodishes.dishgraphs.api.DishApiService
import com.mrdben.autodishes.familygraphs.api.ElderApiService
import com.mrdben.autodishes.ordergraphs.api.OrderApiService

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
    // private const val BASE_URL = "https://www.autodish.top/api/"
    private const val BASE_URL = "http://39.106.169.94:8080/api/"

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
                when (e) {
                    is retrofit2.HttpException -> {
                        Log.w("Retrofit", "Refresh token failed with code: ${e.code()}")
                        if (e.code() == 401 || e.code() == 403) { // Unauthorized or Forbidden
                            Log.w("Retrofit", "Token refresh refused by server, logout.")
                            runBlocking {
                                TokenManager.clearAll()
                                AuthEventBus.postForceLogout()
                            }
                        }
                    }
                    is java.io.IOException -> {
                        Log.e("Retrofit", "Network error during token refresh: ${e.message}")
                    }
                    else -> {
                        Log.e("Retrofit", "Unexpected error during token refresh", e)
                    }
                }
                null
            }
            return newAccessToken
        }
        // refresh token failed. sign out
        runBlocking {
            TokenManager.clearAll()
            AuthEventBus.postForceLogout()
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
                Log.d("Retrofit","refreshToken is empty. proceed without token.")
                return chain.proceed(originalRequest)
            }

            // 3. 确认已有refreshToken，执行请求
            repeat(2) {
                if (accessToken.isNullOrEmpty()) { // actively refresh access token, skip when has access token
                    Log.d("Retrofit","accessToken is empty. refresh it with refreshToken.")
                    if (refreshToken.isNullOrBlank()) {
                        Log.d("Retrofit","refreshToken is empty. Load it from local.")
                        refreshToken = runBlocking { TokenManager.getRefreshToken() }
                    }
                    accessToken = refreshAccessToken(refreshToken)
                    if (accessToken.isNullOrEmpty()) { // cannot refresh access token with refreshToken, return 401 error
                        Log.d("Retrofit","Failed to get accessToken from refreshToken.")
                        return Response.Builder()
                            .request(originalRequest)
                            .protocol(Protocol.HTTP_1_1)
                            .code(401)
                            .message("Token Refresh Failed.")
                            .body("".toResponseBody(null))
                            .build()
                    }
                    Log.d("Retrofit","accessToken refreshed OK with refreshToken.")
                }
                val response = chain.proceed(addTokenToRequest(originalRequest, accessToken)) // try with current access token
                if (response.code == 401) { // the current token may be expired
                    response.close() // 关闭旧响应
                    Log.d("Retrofit","bad accessToken ! try to refresh.")
                    accessToken = null // clear access token
                    TokenManager.clearAccessToken() // clear access token and try again: repeat(2)
                } else {
                    Log.d("Retrofit","Request ok with accessToken.")
                    if (accessToken != savedToken) {
                        Log.d("Retrofit","accessToken updated.")
                        TokenManager.saveAccessToken(accessToken)
                    }
                    AuthWatchDog.resetTimer()
                    return response // success
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
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
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
    val elderApiService: ElderApiService by lazy {
        retrofit.create(ElderApiService::class.java)
    }
    val dishApiService: DishApiService by lazy {
        retrofit.create(DishApiService::class.java)
    }
    val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
}
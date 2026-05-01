package com.mrdben.autodishes.api


import android.content.Context
import com.mrdben.autodishes.authgraphs.api.ApiService
import com.mrdben.autodishes.authgraphs.api.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://39.106.169.94:8080/"

    // 必须持有 Context 才能读取 DataStore，这里通过 lateinit 初始化
    private lateinit var appContext: Context

    // 在 Application 或 MainActivity 的 onCreate 中调用此方法初始化
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun addTokenToRequest(originalRequest: okhttp3.Request, accessToken: String) : okhttp3.Request {
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
    private fun refreshAccessToken() : String? {
        // 同步获取 Refresh Token (注意：拦截器里不能直接挂起，这里简化处理，实际建议用 runBlocking)
        val refreshToken = runBlocking { TokenManager.getRefreshToken(appContext) }

        if (!refreshToken.isNullOrEmpty()) {
            // 调用刷新接口 (这里需要创建一个同步的 Retrofit 实例或者特殊处理，为简化代码，这里示意逻辑)
            // 实际开发中，建议创建一个不带拦截器的 Retrofit 实例专门用来刷新 Token，防止死循环
            val newAccessToken = refreshAccessTokenSync(refreshToken)

            if (newAccessToken != null) {
                // 保存新 Token
                runBlocking {
                    TokenManager.saveAccessToken(newAccessToken)
                }
            }
            return newAccessToken
        }
        return null
    }
    // 认证拦截器：负责给 URL 添加 token 查询参数
    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // 1. 获取当前存储的 Token
            val accessToken = TokenManager.getAccessToken()

            // 2. 如果没有 Token，就直接放行原始请求（比如登录、注册等不需要鉴权的接口）
            if (accessToken.isNullOrEmpty()) {
                return chain.proceed(originalRequest)
            }

            // 5. 执行请求
            val response = chain.proceed(addTokenToRequest(originalRequest, accessToken))

            // 4. 如果返回 401，说明 Token 过期，尝试刷新
            if (response.code == 401) {
                response.close() // 关闭旧响应
                val newAccessToken = refreshAccessToken()
                if (!newAccessToken.isNullOrEmpty()) {
                    return chain.proceed(addTokenToRequest(originalRequest, newAccessToken))
                }
            }
            return response //normal
        }
    }

    // 专门用于刷新 Token 的 Retrofit 实例（不带拦截器，防止死循环）
    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val refreshService = refreshRetrofit.create(ApiService::class.java)

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

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // 同步刷新 Access Token 的方法
    // 注意：拦截器中无法使用 suspend 函数，所以这里需要用 runBlocking 包装
    fun refreshAccessTokenSync(refreshToken: String): String? {
        return try {
            val response = runBlocking {
                refreshService.refreshToken(RefreshTokenRequest(refreshToken))
            }
            response.accessToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
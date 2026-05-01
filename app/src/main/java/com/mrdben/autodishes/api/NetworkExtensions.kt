package com.mrdben.autodishes.api

import retrofit2.HttpException
import java.io.IOException

/**
 * 一个通用的类，用于包装网络请求的结果。
 * 它有两个状态：成功 (Success) 和 失败 (Error)。
 */
sealed class ResultWrapper<out T> {
    // 成功状态，携带返回的数据
    data class Success<out T>(val data: T) : ResultWrapper<T>()

    // 失败状态，携带一个 ApiError 对象来描述错误
    data class Error(val apiError: ApiError) : ResultWrapper<Nothing>()
}

/**
 * 一个数据类，用于标准化 API 返回的错误信息。
 * 这样，无论后端返回何种 HTTP 状态码，UI 层都能以统一的方式处理。
 */
data class ApiError(
    val statusCode: Int,      // HTTP 状态码，如 400, 404
    val message: String       // 用户友好的错误信息
)
/**
 * 一个扩展函数，用于安全地执行 suspend 类型的 Retrofit 调用。
 * 它会自动捕获异常并将其转换为 ResultWrapper.Error。
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultWrapper<T> {
    return try {
        // 尝试执行网络请求
        val response = apiCall()
        ResultWrapper.Success(response)
    } catch (e: HttpException) {
        // 捕获 HTTP 错误 (4xx, 5xx)
        // e.code() 获取状态码，如 400
        // e.response()?.errorBody()?.string() 获取后端返回的错误体
        val errorMessageRaw = e.message() // 或者从 errorBody 中解析更详细的 message
        val errorMessage = try {
            val json = e.response()?.errorBody()?.string()
            json?.let {
                val gson = com.google.gson.Gson()
                val jsonObject = gson.fromJson(it, com.google.gson.JsonObject::class.java)
                jsonObject.get("detail")?.asString ?: errorMessageRaw
            } ?: errorMessageRaw
        } catch (ex: Exception) {errorMessageRaw}
        ResultWrapper.Error(ApiError(e.code(), errorMessage))
    } catch (e: IOException) {
        // 捕获网络问题，如断网、超时
        ResultWrapper.Error(ApiError(-1, "网络连接失败，请检查网络设置"))
    } catch (e: Exception) {
        // 捕获其他所有未知异常
        ResultWrapper.Error(ApiError(-1, "发生未知错误: ${e.message}"))
    }
}
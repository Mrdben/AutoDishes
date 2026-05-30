package com.mrdben.autodishes.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


object AuthWatchDog {
    // 独立的协程作用域，不依赖任何页面或组件
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null
    private var watchdogFlag: Boolean = true

    // 间隔时间（毫秒）
    private const val INTERVAL_MS = 60 * 1000L

    init { startTimer() }

    fun startTimer() {
        job?.cancel() // 取消当前正在倒计时的任务
        job = scope.launch {
            Log.d("AuthWD","Watch dog timer started.")
            while (isActive) { // 死循环
                delay(INTERVAL_MS) // 等待间隔时间
                Log.d("AuthWD","Watch dog timer fired.")
                if (TokenManager.hasRefreshToken)
                    checkFlag() // 时间到了，看一眼标识位
            }
        }
    }

    private suspend fun checkFlag() {
        if (watchdogFlag) {
            Log.d("AuthWD","Watch dog activated, start API call.")
            safeApiCall{ RetrofitClient.authApiService.userAlive() }
        }
        watchdogFlag = true
    }

    fun resetTimer() {
        Log.d("AuthWD","Watch dog flag cleared.")
        watchdogFlag = false
    }
}
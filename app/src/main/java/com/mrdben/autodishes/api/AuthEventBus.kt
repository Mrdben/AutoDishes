package com.mrdben.autodishes.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventBus {
    // MutableSharedFlow 用于内部发送事件，replay = 0 表示不重放旧事件，extraBufferCapacity = 1 防止并发冲突
    private val _forceLogoutFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    // 对外暴露只读的 Flow
    val forceLogoutFlow = _forceLogoutFlow.asSharedFlow()
    // 记录上一次触发强制登出的时间戳
    private var lastLogoutTime = 0L
    // 设置防抖间隔，比如 3000 毫秒（3秒）内只允许触发一次
    private const val DEBOUNCE_INTERVAL = 3000L
    // 供网络层调用的发送方法
    suspend fun postForceLogout() {
        val currentTime = System.currentTimeMillis()
        // 只有当距离上次触发超过设定的时间间隔时，才允许发送新的事件
        if (currentTime - lastLogoutTime > DEBOUNCE_INTERVAL) {
            lastLogoutTime = currentTime
            _forceLogoutFlow.emit(Unit)
        }
    }
}
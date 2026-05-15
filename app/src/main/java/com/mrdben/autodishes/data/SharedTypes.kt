package com.mrdben.autodishes.data

enum class SyncStatus {
    SYNCED,       // 已同步
    TO_UPLOAD,    // 待上传（新增/修改）
    TO_DELETE     // 待删除
}

sealed class WorkState {
    object Idle : WorkState() // 初始状态
    object Loading : WorkState() // 登录中
    data class Success(val message: String) : WorkState() // 成功
    data class Error(val message: String) : WorkState() // 失败
}
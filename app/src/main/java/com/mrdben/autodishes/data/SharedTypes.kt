package com.mrdben.autodishes.data

enum class SyncStatus {
    SYNCED,       // 已同步
    TO_UPLOAD,    // 待上传（新增/修改）
    TO_DELETE     // 待删除
}
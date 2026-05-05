package com.mrdben.autodishes.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun getCurrentUtcTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

fun utcToLocalTime(rawUtcStr: String): String {
    if (rawUtcStr.isBlank()) return ""
    val utcStr = rawUtcStr.replace(" ","T")
    // 1. 解析器：指定时区为UTC
    val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = utcSdf.parse(utcStr)
    Log.d("TimeTest", "【解析后的时间(UTC)】: $date")

    // 2. 格式化器：本地时区
    val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val result = localSdf.format(date!!)
    Log.d("TimeTest", "【转换后的本地时间】: $result")

    return try {
        // 解析UTC时间 -> 转为本地时间字符串
        val date = utcSdf.parse(utcStr)
        date?.let { localSdf.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}
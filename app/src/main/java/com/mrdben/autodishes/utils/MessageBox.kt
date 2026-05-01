package com.mrdben.autodishes.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DialogType {
    SUCCESS,    // 确认/成功
    ERROR,      // 错误
    WARNING,    // 告警
    LOADING,    // 等待/加载
    INFO        // 普通信息
}

// 扩展属性：根据类型获取对应的图标
val DialogType.icon: ImageVector
    get() = when (this) {
        DialogType.SUCCESS -> Icons.Default.CheckCircle
        DialogType.ERROR -> Icons.Default.Error
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.LOADING -> Icons.Default.Update // 或者用旋转的加载图标
        DialogType.INFO -> Icons.Default.Info
    }
// 如果是加载类型，启动旋转动画

// 扩展属性：根据类型获取对应的颜色
val DialogType.color: Color
    get() = when (this) {
        DialogType.SUCCESS -> Color(0xFF4CAF50) // 绿色
        DialogType.ERROR -> Color(0xFFE53935)   // 红色
        DialogType.WARNING -> Color(0xFFFF9800) // 橙色
        DialogType.LOADING -> Color.Gray        // 灰色
        DialogType.INFO -> Color.Blue           // 蓝色
    }
@Composable
fun MessageDialog(
    title: String,
    message: String,
    type: DialogType = DialogType.INFO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDismiss: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    AlertDialog(
        onDismissRequest = onDismiss, // 处理对话框外部的点击
        title = {
            Text(text = title, fontSize = 22.sp)
        },
        text = {
            Text(text = message, fontSize = 18.sp)
        },
        icon = {Icon(
            imageVector = type.icon,
            tint = type.color,          // 设置颜色
            modifier = Modifier.size(36.dp)
                .rotate(if (type == DialogType.LOADING) rotation else 0f), // 设置大小
            contentDescription = "icon"
        )},
        confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "确定", fontSize = 18.sp)
                }
        },
        dismissButton = if (showDismiss) {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消", fontSize = 18.sp)
                }
            }
        } else { null }
    )
}
package com.mrdben.autodishes.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun CountdownButton(
    // 暴露给外部页面的参数
    modifier: Modifier = Modifier,
    initialText: String = "点击获取验证码",     // 初始/结束时的文字
    enabled: Boolean = true,
    countdownSeconds: Int = 60,            // 倒计时的总秒数
    onClickAction: () -> Unit = {},        // 点击按钮时触发的业务逻辑（如发送网络请求）
    onCountdownFinish: () -> Unit = {}     // 倒计时完全结束后的回调（可选）
) {
    // 内部维护的私有状态
    var isCountingDown by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(countdownSeconds) }

    // 倒计时核心逻辑
    LaunchedEffect(key1 = isCountingDown) {
        if (isCountingDown && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                remainingSeconds--
            }
            // 倒计时自然结束
            isCountingDown = false
            remainingSeconds = countdownSeconds
            onCountdownFinish.invoke() // 触发结束回调
        }
    }

    // 动态计算按钮文字
    val buttonText = if (isCountingDown) "${remainingSeconds}秒后重试" else initialText
    val buttenEnabled = enabled && !isCountingDown
    TextButton(
        onClick = {
            onClickAction()
            isCountingDown = true
        },
        modifier = modifier,
        enabled = buttenEnabled
    ) {
        Text(
            text = buttonText,
            fontSize = 18.sp,
            color = if (buttenEnabled)
                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
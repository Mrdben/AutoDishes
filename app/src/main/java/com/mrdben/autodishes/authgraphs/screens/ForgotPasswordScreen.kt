package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit
) {
    // 页面状态管理（仅UI绑定，无业务逻辑）
    var phoneNumber by remember { mutableStateOf("") } // 手机号
    var verifyCode by remember { mutableStateOf("") } // 验证码
    var validPassword by remember { mutableStateOf("") } // 新密码
    var isPhoneNumberValid by remember { mutableStateOf(false) } // 验证码按钮是否可点击
    isPhoneNumberValid = phoneNumber.length == 11 // 简单校验手机号长度

    // 页面整体布局（和注册页一致的风格）
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 页面标题
        Text(
            text = "忘记密码",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 1. 手机号输入框
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { inputText -> phoneNumber = inputText },
            label = { Text("请输入注册手机号", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone // 手机号键盘
            ),
            textStyle = TextStyle(fontSize = 18.sp), // 老年适配：大字号
            placeholder = { Text("例如：13800138000", fontSize = 16.sp) },
            isError = !isPhoneNumberValid
        )

        Spacer(modifier = Modifier.size(16.dp))

        // 2. 验证码区域（输入框 + 获取验证码按钮）
        Column(modifier = Modifier.fillMaxWidth()) {
            // 验证码输入框
            OutlinedTextField(
                value = verifyCode,
                onValueChange = { inputCode -> verifyCode = inputCode },
                label = { Text("请输入验证码", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // 数字键盘
                ),
                textStyle = TextStyle(fontSize = 18.sp)
            )

            // 获取验证码按钮（靠右显示，和输入框拉开间距）
            TextButton(
                onClick = {
                    // 仅占位：后续实现“发送验证码”逻辑
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                enabled = isPhoneNumberValid
            ) {
                Text(
                    text = "获取验证码",
                    fontSize = 16.sp,
                    color = if (isPhoneNumberValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        // 3. 新密码输入框
        PasswordSettingSection(
            onValidPasswordChange = { validPwd ->
                validPassword = validPwd
            }
        )

        Spacer(modifier = Modifier.size(32.dp))

        // 5. 重置密码按钮（核心操作按钮）
        Button(
            onClick = {
                // 仅占位：后续实现“校验验证码+重置密码”逻辑
            },
            modifier = Modifier.fillMaxWidth(),
            // 简单UI校验：所有必填项非空才激活按钮
            enabled = phoneNumber.isNotBlank() &&
                    verifyCode.isNotBlank() &&
                    validPassword.isNotBlank()
        ) {
            Text(
                text = "重置密码",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 6. 返回登录按钮（辅助操作）
        TextButton(
            onClick = {
                onNavigateToLogin()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "想起密码了？返回登录",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

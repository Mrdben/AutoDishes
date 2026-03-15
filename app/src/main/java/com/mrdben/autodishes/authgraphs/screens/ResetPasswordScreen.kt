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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResetPasswordScreen(
    // 外部传入用户名和手机号（模拟已登录/已验证的用户信息）
    username: String = "老厨友123",
    phoneNumber: String = "138****8888" // 脱敏展示，符合隐私规范
) {
    // 页面状态管理（仅UI绑定，无业务逻辑）
    var oldPassword by remember { mutableStateOf("") } // 旧密码
    var verifyCode by remember { mutableStateOf("") } // 手机验证码
    var validPassword by remember { mutableStateOf("") } // 新密码

    // 页面整体布局（和注册/忘记密码页一致的风格）
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
            text = "重置密码",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 1. 用户信息展示区域（不可编辑，仅展示）
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "当前账号",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            // 用户名展示
            Text(
                text = "用户名：$username",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // 手机号展示（脱敏）
            Text(
                text = "绑定手机：$phoneNumber",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        // 2. 旧密码输入框
        OutlinedTextField(
            value = oldPassword,
            onValueChange = { inputPwd -> oldPassword = inputPwd },
            label = { Text("请输入旧密码", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            textStyle = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        // 3. 验证码区域（输入框 + 获取验证码按钮）
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

            // 获取验证码按钮（靠右显示）
            TextButton(
                onClick = {
                    // 仅占位：后续实现“发送验证码到绑定手机”逻辑
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
            ) {
                Text(
                    text = "获取验证码",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        PasswordSettingSection(
            onValidPasswordChange = { validPwd ->
                validPassword = validPwd
            }
        )
        Spacer(modifier = Modifier.size(32.dp))

        // 6. 确认重置按钮（核心操作按钮）
        Button(
            onClick = {
                // 仅占位：后续实现“校验旧密码+验证码+重置密码”逻辑
            },
            modifier = Modifier.fillMaxWidth(),
            // 简单UI校验：所有必填项非空才激活按钮
            enabled = oldPassword.isNotBlank() &&
                    verifyCode.isNotBlank() &&
                    validPassword.isNotBlank()
        ) {
            Text(
                text = "确认重置",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 7. 取消按钮（辅助操作）
        TextButton(
            onClick = {
                // 仅占位：后续实现“返回上一页”的导航逻辑
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "取消",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

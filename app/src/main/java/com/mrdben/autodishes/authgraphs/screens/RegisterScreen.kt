package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun RegisterScreen() {
    var username by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var validPassword by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var isPhoneNumberValid by remember { mutableStateOf(false) } // 验证码按钮是否可点击
    isPhoneNumberValid = phonenumber.length == 11 // 简单校验手机号长度

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "暖心厨房 - 用户注册",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        // input username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("请输入用户名", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.size(18.dp))
        // input phone number
        OutlinedTextField(
            value = phonenumber,
            onValueChange = { phonenumber = it },
            label = { Text("请输入手机号码", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            isError = !isPhoneNumberValid
        )
        Spacer(modifier = Modifier.size(18.dp))
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
        Spacer(modifier = Modifier.size(18.dp))
        // input password
        PasswordSettingSection(
            onValidPasswordChange = { validPwd ->
                validPassword = validPwd
            }
        )
        Spacer(modifier = Modifier.size(32.dp))

        // registry button
        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank()
                    && phonenumber.isNotBlank()
                    && validPassword.isNotBlank()
        ) {
            Text(
                text = "注册",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    // 注册成功弹窗（调试用）
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("注册信息（调试）") },
            text = {
                Column {
                    Text(text = "用户名：$username", fontSize = 16.sp)
                    Text(text = "手机号：$phonenumber", fontSize = 16.sp)
                    Text(text = "密码：$validPassword", fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("确定", fontSize = 16.sp)
                }
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun RegisterScreenPreview() {
    RegisterScreen()
}
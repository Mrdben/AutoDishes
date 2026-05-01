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
import androidx.compose.runtime.collectAsState
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

import androidx.lifecycle.viewmodel.compose.viewModel

import com.mrdben.autodishes.authgraphs.viewmodel.AuthState
import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog
import com.mrdben.autodishes.utils.ValidationUtils
import com.mrdben.autodishes.utils.ValidationResult

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var validPassword by remember { mutableStateOf("") }

    var isUsernameValid by remember { mutableStateOf(ValidationResult() ) }
    var isPhoneNumberValid by remember { mutableStateOf(ValidationResult()) }
    isUsernameValid = ValidationUtils.validateUsername(username)
    isPhoneNumberValid = ValidationUtils.validatePhone(phoneNumber)

    val uiState by authViewModel.authState.collectAsState()

    //val context = LocalContext.current
    when (val currentState = uiState) {
        is AuthState.Success -> {
            MessageDialog(
                title = "用户注册成功",
                message = "用户：${username} 注册成功\n请点击登录按钮进行用户登录。",
                type = DialogType.SUCCESS,
                onDismiss = {},
                onConfirm = {
                    authViewModel.setAuthState(AuthState.Idle)
                    onSuccess()
                },
                showDismiss = false
            )
        }
        is AuthState.Error -> {
            MessageDialog(
                title = "用户注册失败",
                message = "用户注册失败：\n ${currentState.message}\n请重试。",
                type = DialogType.ERROR,
                onDismiss = {},
                onConfirm = { authViewModel.setAuthState(AuthState.Idle) },
                showDismiss = false
            )
        }
        is AuthState.Loading -> {
            MessageDialog(
                title = "努力工作中",
                message = "页面加载中，请稍后。。。",
                type = DialogType.LOADING,
                onDismiss = {},
                onConfirm = {},
                showDismiss = false
            )
        }
        else -> {} //loading 状态下，可以显示等待图标
    }

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
            ),
            isError = !isUsernameValid.isValid,
            supportingText = {
                Text(text = isUsernameValid.errorMessage ?: "",
                    fontSize = 12.sp)
            }
        )

        Spacer(modifier = Modifier.size(18.dp))
        // input phone number
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("请输入手机号码", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            isError = !isPhoneNumberValid.isValid,
            supportingText = {
                Text(text = isPhoneNumberValid.errorMessage ?: "",
                    fontSize = 12.sp)
            }
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
                enabled = isPhoneNumberValid.isValid
            ) {
                Text(
                    text = "获取验证码",
                    fontSize = 16.sp,
                    color = if (isPhoneNumberValid.isValid)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                authViewModel.register(username, validPassword, phoneNumber)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isUsernameValid.isValid
                    && isPhoneNumberValid.isValid
                    && validPassword.isNotBlank()
        ) {
            Text(
                text = "注册",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        // registry button
        Button(
            onClick = {
                onCancel()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "取消",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

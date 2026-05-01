package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter
import coil.compose.AsyncImage

import com.mrdben.autodishes.R
import com.mrdben.autodishes.authgraphs.viewmodel.AuthState
import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog

// 页面UI组件
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToRegistry: () -> Unit,
    onNavigateToForgetPassword: () -> Unit,
    onSignOn: () -> Unit
) {
    // 用户名输入状态
    var username by remember { mutableStateOf("") }
    // 密码输入状态
    var password by remember { mutableStateOf("") }
    // 密码是否可见
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        snapshotFlow { authViewModel.loginUser.value }
            .filter { it.isNotEmpty() }
            .collect { fetchedName ->
                username = fetchedName
            }
    }

    when (val currentState = uiState) {
        is AuthState.Success -> {
            authViewModel.setAuthState(AuthState.Idle)
            onSignOn()
        }
        is AuthState.Error -> {
            MessageDialog(
                title = "登录失败",
                message = "用户登录失败：\n ${currentState.message}\n请重试。",
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
        else -> {}
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = R.drawable.home_background_lg, // 本地大图片资源
            contentDescription = "home page background",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center), // 背景图全屏
            contentScale = ContentScale.FillHeight, // 关键：左右撑满居中，上下拉伸/压缩
            alignment = Alignment.Center, // 确保图片左右居中
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .clip(MaterialTheme.shapes.medium) // 配合shape裁剪，避免背景圆角失效
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 用户名输入框
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text
                    )
                )
                Spacer(modifier = Modifier.size(16.dp))

                // 密码输入框（支持显示/隐藏）
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation() // 密码隐藏为圆点/星号
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        // 密码可见性切换按钮
                        val icon = if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        }
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "切换密码可见性")
                        }
                    }
                )
                Spacer(modifier = Modifier.size(24.dp))

                // 登录按钮
                Button(
                    onClick = {
                        authViewModel.login(username, password)
                    }, // 点击弹出提示框
                    modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text("登录", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.size(8.dp))

                // 注册按钮
                Button(
                    onClick = { onNavigateToRegistry() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("注册", style = MaterialTheme.typography.bodyLarge)
                }

                // 忘记密码文字链接（非按钮样式，可点击跳转）
                Text(
                    text = "忘记密码？",
                    modifier = Modifier
                        .padding(top = 12.dp) // 和上方按钮拉开间距
                        .clickable {
                            // 点击后跳转到忘记密码页面（调用你定义的导航回调）
                            onNavigateToForgetPassword()
                        },
                    color = MaterialTheme.colorScheme.primary, // 用主题主色，醒目但不刺眼
                    fontSize = 16.sp, // 适配长辈阅读的字号
                    textAlign = TextAlign.Center // 文字居中
                )
            }
        }
    }
}
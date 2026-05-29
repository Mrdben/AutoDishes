package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.utils.*
import com.mrdben.autodishes.MyApplication
import com.mrdben.autodishes.data.WorkState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(factory = MyApplication.Factory),
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var validPassword by remember { mutableStateOf("") }

    var isUsernameValid by remember { mutableStateOf(ValidationResult()) }
    var isPhoneNumberValid by remember { mutableStateOf(ValidationResult()) }
    isUsernameValid = ValidationUtils.validateUsername(username)
    isPhoneNumberValid = ValidationUtils.validatePhone(phoneNumber)

    val uiState by authViewModel.authState.collectAsState()
    when (val currentState = uiState) {
        is WorkState.Success -> {
            MessageDialog(
                title = "用户注册成功",
                message = "用户：${username} 注册成功\n请点击登录按钮进行用户登录。",
                type = DialogType.SUCCESS,
                onDismiss = {},
                onConfirm = {
                    authViewModel.setWorkState(WorkState.Idle)
                    onSuccess()
                },
                showDismiss = false
            )
        }
        is WorkState.Error -> {
            MessageDialog(
                title = "用户注册失败",
                message = "用户注册失败：\n ${currentState.message}\n请重试。",
                type = DialogType.ERROR,
                onDismiss = {},
                onConfirm = { authViewModel.setWorkState(WorkState.Idle) },
                showDismiss = false
            )
        }
        is WorkState.Loading -> {
            MessageDialog(
                title = "努力工作中",
                message = "页面加载中，请稍后。。。",
                type = DialogType.LOADING,
                onDismiss = {},
                onConfirm = {},
                showDismiss = false
            )
        }
        is WorkState.Info -> {
            MessageDialog(
                title = "信息提示",
                message = currentState.message,
                type = DialogType.INFO,
                onDismiss = {},
                onConfirm = { authViewModel.setWorkState(WorkState.Idle) },
                showDismiss = false
            )
        }
        else -> {} //loading 状态下，可以显示等待图标
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "暖心厨房 - 用户注册") },
                // 返回按钮
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        authViewModel.register(username, validPassword, phoneNumber, verifyCode)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isUsernameValid.isValid
                            && isPhoneNumberValid.isValid
                            && validPassword.isNotBlank()
                ) {
                    Text("注册", fontSize = 18.sp)
                }
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消", fontSize = 18.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val spacerSize = 16.dp
            // input username
            Spacer(modifier = Modifier.size(spacerSize))
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

            // input phone number
            Spacer(modifier = Modifier.size(spacerSize))
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

            // 2. 验证码区域（输入框 + 获取验证码按钮）
            // 验证码输入框
            //Spacer(modifier = Modifier.size(spacerSize))
            OutlinedTextField(
                value = verifyCode,
                onValueChange = { verifyCode = it },
                label = { Text("请输入验证码", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number // 数字键盘
                ),
                textStyle = TextStyle(fontSize = 18.sp)
            )
            CountdownButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                onClickAction = { authViewModel.validationCodeRequest(phoneNumber, "register") },
                enabled = isPhoneNumberValid.isValid,
                initialText = "点击获取验证码"
            )

            Spacer(modifier = Modifier.size(spacerSize))
            // input password
            PasswordSettingSection(
                onValidPasswordChange = { validPassword = it }
            )
        }
    }
}

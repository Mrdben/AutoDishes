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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.authgraphs.viewmodel.AuthState
import com.mrdben.autodishes.utils.*
import com.mrdben.autodishes.MyApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserScreen(
    style: String = "password",
    onBackClick:() -> Unit,
    onLogoff:() -> Unit,
    viewModel: AuthViewModel = viewModel(factory = MyApplication.Factory)
) {
    // config page behavior according to style
    var pageTitle = ""
    var usernameEnable = false
    var phoneNumberEnable = true
    var showOldPassword = true
    var showNewPassword = true
    var showWarning by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var validPassword by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var isPhoneNumberValid by remember { mutableStateOf(ValidationResult()) }
    isPhoneNumberValid = ValidationUtils.validatePhone(phoneNumber)
    var passwordVisible by remember { mutableStateOf(false) }
    var isConfirmButtonEnabled by remember { mutableStateOf(false) }

    when (style) {
        "password" -> {
            pageTitle = "修改密码"
            usernameEnable = false
            phoneNumberEnable = false
            showOldPassword = true
            showNewPassword = true
            isConfirmButtonEnabled = oldPassword.isNotBlank()
                                  && validPassword.isNotBlank()
        }
        "phone" -> {
            pageTitle = "更新手机号码"
            usernameEnable = false
            phoneNumberEnable = true
            showOldPassword = true
            showNewPassword = false
            isConfirmButtonEnabled = oldPassword.isNotBlank()
                                  && isPhoneNumberValid.isValid
        }
        "forgot" -> {
            pageTitle = "重置密码"
            usernameEnable = true
            phoneNumberEnable = true
            showOldPassword = false
            showNewPassword = true
            isConfirmButtonEnabled = username.isNotBlank()
                                  && validPassword.isNotBlank()
                                  && isPhoneNumberValid.isValid
        }
        "delete" -> {
            pageTitle = "注销用户"
            usernameEnable = false
            phoneNumberEnable = false
            showOldPassword = true
            showNewPassword = false
            isConfirmButtonEnabled = oldPassword.isNotBlank()
        }
    }
    val userProfile = viewModel.userProfile.collectAsStateWithLifecycle()
    LaunchedEffect(userProfile) {
        username = userProfile.value.username
        phoneNumber = if (!phoneNumberEnable) userProfile.value.phoneNumber else ""
    }
    val uiState by viewModel.authState.collectAsStateWithLifecycle()
    when (val currentState = uiState) {
        is AuthState.Success -> {
            MessageDialog(
                title = "操作成功！",
                message = "$pageTitle 成功！",
                type = DialogType.SUCCESS,
                onDismiss = {},
                onConfirm = {
                    viewModel.setAuthState(AuthState.Idle)
                    viewModel.logout()
                    onLogoff()
                },
                showDismiss = false
            )
        }
        is AuthState.Error -> {
            MessageDialog(
                title = "操作失败",
                message = "$pageTitle 失败！\n ${currentState.message}\n请重试。",
                type = DialogType.ERROR,
                onDismiss = {},
                onConfirm = { viewModel.setAuthState(AuthState.Idle) },
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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = pageTitle) },
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
                        when (style) {
                            "password" -> {
                                viewModel.changePassword(
                                    oldPassword = oldPassword,
                                    newPassword = validPassword,
                                    phoneNumber = phoneNumber
                                )
                            }
                            "phone" -> {
                                viewModel.updatePhoneNumber(
                                    password = oldPassword,
                                    newPhoneNumber = phoneNumber
                                )
                            }
                            "delete" -> {
                                showWarning = true
                            }
                            "forgot" -> {
                                viewModel.resetPassword(
                                    username = username,
                                    phoneNumber = phoneNumber,
                                    newPassword = validPassword
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isConfirmButtonEnabled
                ) {
                    Text("确定", fontSize = 18.sp)
                }
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("返回", fontSize = 18.sp)
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
            Spacer(modifier = Modifier.size(spacerSize))
            // 用户名
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                ),
                textStyle = TextStyle(fontSize = 18.sp),
                enabled = usernameEnable
            )

            // 电话号码
            Spacer(modifier = Modifier.size(spacerSize))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("手机号码", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = TextStyle(fontSize = 18.sp),
                isError = !isPhoneNumberValid.isValid,
                supportingText = {
                    if (!isPhoneNumberValid.errorMessage.isNullOrBlank()) {
                        Text(
                            text = isPhoneNumberValid.errorMessage!!,
                            fontSize = 12.sp
                        )
                    }
                },
                enabled = phoneNumberEnable
            )

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
            // 短信验证码
            // 获取验证码按钮（靠右显示，和输入框拉开间距）
            TextButton(
                onClick = {
                    // 仅占位：后续实现“发送验证码”逻辑
                },
                modifier = Modifier.align(Alignment.End),
                enabled = isPhoneNumberValid.isValid
            ) {
                Text(
                    text = "获取验证码",
                    fontSize = 16.sp,
                    color = if (isPhoneNumberValid.isValid)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            // 原始密码输入框
            Spacer(modifier = Modifier.size(spacerSize))
            if (showOldPassword) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("原始密码", fontSize = 18.sp) },
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
            }
            // 新密码输入框
            Spacer(modifier = Modifier.size(spacerSize))
            if (showNewPassword) {
                // input password
                PasswordSettingSection(
                    onValidPasswordChange = { validPassword = it }
                )
            }
        }
    }
    if (showWarning) {
        MessageDialog(
            title = "删除用户",
            message = "彻底删除【${username}】的用户数据，该操作不可恢复。\n请三思而后行。\n是否真的彻底删除账户：${username}？",
            type = DialogType.WARNING,
            onDismiss = { showWarning = false },
            onConfirm = {
                showWarning = false
                viewModel.deleteAccount(password = oldPassword,
                                        phoneNumber = phoneNumber)
            },
            showDismiss = true
        )
    }
}

package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.MyApplication
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.utils.utcToLocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    onChangePassword: () -> Unit,
    onUpdatePhoneNumber: () -> Unit,
    onLogoff:() -> Unit,
    onDeleteUser:() -> Unit,
    viewModel: AuthViewModel = viewModel(factory = MyApplication.Factory)
) {
    val userProfile = viewModel.userProfile.collectAsStateWithLifecycle()
    val elders = viewModel.elders.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 在这里调用你要执行的 ViewModel 函数
        viewModel.getUserProfileFromServer()
        //viewModel.syncFamilyWithServer()
    }
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "用户资料") },
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
                    onClick = onChangePassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("修改密码", fontSize = 18.sp)
                }
                Button(
                    onClick = onUpdatePhoneNumber,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("更新手机号码", fontSize = 18.sp)
                }
                Button(
                    onClick = {showRenameDialog = true},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("更新家庭名称", fontSize = 18.sp)
                }
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogoff()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("退出登录", fontSize = 18.sp)
                }
                Button(
                    onClick = onDeleteUser,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError // 自动白色
                    )
                ) {
                    Text("注销用户", fontSize = 18.sp)
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
        // 主内容区（可滚动，适配小屏幕）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (TokenManager.getAccessToken().isNullOrBlank()) {
                Text(
                    text = "网络故障，无法获取用户资料。",
                    color = MaterialTheme.colorScheme.error
                )
            }
            // 老人信息卡片（核心展示区）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "用户名： ${userProfile.value.username}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "家庭名称： ${userProfile.value.familyName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "成员数量：${elders.value.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "注册编号： user_%04d".format(userProfile.value.userId),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "注册手机号： ${userProfile.value.phoneNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "注册时间：${utcToLocalTime(userProfile.value.registryTime)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
    if (showRenameDialog) {
        FamilyRenameDialog(
            originalName = userProfile.value.familyName, // 传递原名称
            onDismiss = { showRenameDialog = false }, // 关闭对话框
            onConfirm = { newName ->
                showRenameDialog = false
                viewModel.updateFamilyName(newName)
            }
        )
    }
}

@Composable
fun FamilyRenameDialog(
    originalName: String, // 原家庭名称（用于回显）
    onDismiss: () -> Unit, // 关闭对话框的回调（取消/点击外部）
    onConfirm: (String) -> Unit // 确定修改的回调（传递新名称）
) {
    // 存储输入框内容（内部维护，主页面无需关心）
    var newFamilyName by remember(originalName) { mutableStateOf(originalName) }

    // 仅当visible为true时显示对话框
    AlertDialog(
        onDismissRequest = {},
        title = { Text("修改家庭名称", fontSize = 20.sp) },
        text = {
            BasicTextField(
                value = newFamilyName,
                onValueChange = { newFamilyName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp
                ),
                decorationBox = { innerTextField ->
                    if (newFamilyName.isEmpty()) {
                        Text(
                            text = "请输入家庭名称",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newFamilyName.isNotBlank()) {
                        onConfirm(newFamilyName) // 传递新名称给主页面
                        onDismiss() // 关闭对话框
                    }
                }
            ) {
                Text("确定", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )

}


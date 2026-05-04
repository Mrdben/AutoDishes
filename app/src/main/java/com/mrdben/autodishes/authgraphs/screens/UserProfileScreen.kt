package com.mrdben.autodishes.authgraphs.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Locale

import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel
import com.mrdben.autodishes.MyApplication
import com.mrdben.autodishes.api.TokenManager

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
    val currentFamily = viewModel.currentFamily.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        // 在这里调用你要执行的 ViewModel 函数
        viewModel.getUserProfileFromServer()
        viewModel.syncFamilyWithServer()
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
                // 家庭信息卡片（核心展示区）
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
                            text = "家庭名称： ${currentFamily.value.familyName}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "注册编号： ${currentFamily.value.familyId}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "注册时间：${utcToLocalTime(currentFamily.value.createdAt)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "老人数量：${currentFamily.value.elderIds.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
            }
            if (TokenManager.getAccessToken().isNullOrBlank()) {
                Text(
                    text = "网络故障，无法获取用户资料。",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun utcToLocalTime(utcStr: String): String {
    if (utcStr.isBlank()) return ""
    // 1. 解析器：指定时区为UTC
    val utcSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = utcSdf.parse(utcStr)
    Log.d("TimeTest", "【解析后的时间(UTC)】: $date")

    // 2. 格式化器：本地时区
    val localSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val result = localSdf.format(date!!)
    Log.d("TimeTest", "【转换后的本地时间】: $result")

    return try {
        // 解析UTC时间 -> 转为本地时间字符串
        val date = utcSdf.parse(utcStr)
        date?.let { localSdf.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}
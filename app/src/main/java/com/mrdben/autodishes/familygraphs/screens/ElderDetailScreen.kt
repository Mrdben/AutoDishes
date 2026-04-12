package com.mrdben.autodishes.familygraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.data.DEFAULT_ELDER
import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.getDisplayName
import com.mrdben.autodishes.familygraphs.viewmodel.FamilyViewModel

/**
 * 老人详情页面
 * @param elderId 要展示的老人数据
 * @param onBackClick 返回上一页的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderDetailScreen(
    elderId: String,
    onBackClick: () -> Unit,
    onEditElder: (String) -> Unit,
    readOnly: Boolean = false,
    viewModel: FamilyViewModel = viewModel()
) {
    var elder by remember {mutableStateOf(DEFAULT_ELDER)}
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(elderId) {
        val fetchedElder = viewModel.getElderById(elderId)
        elder = fetchedElder ?: DEFAULT_ELDER
    }
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "家人详情") },
                // 返回按钮
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                if (readOnly) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("返回", fontSize = 18.sp)
                    }
                } else {

                    Button(
                        onClick = { onEditElder(elderId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("编辑", fontSize = 18.sp)
                    }
                    Button(
                        onClick = {
                            // 只打开弹窗，不直接删除
                            showDeleteConfirm = true
                            //viewModel.removeElder(elderId)
                            //onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("删除", fontSize = 18.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        // 主内容区（可滚动，适配小屏幕）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp)
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
                    // 1. 姓名（突出显示）
                    Text(
                        text = elder.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 2. 年龄（补空格对齐，保持和列表页一致的样式）
                    Text(
                        text = "年龄：${elder.age.toString().padStart(3, ' ')}岁",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    // 3. 性别
                    Text(
                        text = "性别：${elder.gender.getDisplayName()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "---- 健康信息 ----",
                        modifier = Modifier
                            .fillMaxWidth(),// 让文本行占满父布局宽度（居中的前提）
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center // 核心：文本水平居中
                    )
                    // 4. 过敏源展示
                    Text(
                        text = "过敏源：${if (elder.allergens.isNotEmpty()) elder.allergens.joinToString("、") else "无"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    // 5. 疾病展示
                    Text(
                        text = "疾病：${if (elder.diseases.isNotEmpty()) elder.diseases.joinToString("、") else "无"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "---- 饮食偏好 ----",
                        modifier = Modifier
                            .fillMaxWidth(),// 让文本行占满父布局宽度（居中的前提）
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center // 核心：文本水平居中
                    )
                    // 6. 忌口展示
                    Text(
                        text = "忌口：${if (elder.dietaryRestrictions.isNotEmpty()) elder.dietaryRestrictions.joinToString("、") else "无"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    // 7. 口味偏好展示
                    Text(
                        text = "口味偏好：${if (elder.tastePreferences.isNotEmpty()) elder.tastePreferences.joinToString("、") else "无"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    // 8. 老人ID（可选展示，便于调试/管理）
                    Text(
                        text = "编号：${elder.elderId}",
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
    // 弹窗
    DeleteElderConfirmDialog(
        elder = elder,
        visible = showDeleteConfirm,
        onConfirm = {
            // 确认删除
            showDeleteConfirm = false
            viewModel.removeElder(elderId)
            onBackClick()
        },
        onDismiss = {
            showDeleteConfirm = false
        }
    )
}

@Composable
fun DeleteElderConfirmDialog(
    elder: Elder?,
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible || elder == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = {
            Column {
                Text("确定要删除该老人信息吗？", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("姓名：${elder.name}", fontSize = 18.sp)
                Text("性别：${elder.gender.getDisplayName()}", fontSize = 18.sp)
                Text("年龄：${elder.age}", fontSize = 18.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", fontSize = 18.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontSize = 18.sp)
            }
        }
    )
}
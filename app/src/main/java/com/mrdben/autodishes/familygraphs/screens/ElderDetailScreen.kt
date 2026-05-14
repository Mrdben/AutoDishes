package com.mrdben.autodishes.familygraphs.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.MyApplication
import com.mrdben.autodishes.familygraphs.data.DEFAULT_ELDER
import com.mrdben.autodishes.familygraphs.viewmodel.FamilyViewModel
import com.mrdben.autodishes.familygraphs.data.getDisplayName
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog
import com.mrdben.autodishes.utils.utcToLocalTime

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
    viewModel: FamilyViewModel = viewModel(factory = MyApplication.Factory)
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
                            showDeleteConfirm = true
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 8. 老人ID（可选展示，便于调试/管理）
                        Text(
                            text = "成员编号：${elder.elderId}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "用户编号：${elder.userId}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "创建时间：${utcToLocalTime(elder.createdAt)}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "修改时间：${utcToLocalTime(elder.updatedAt)}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }
    // 弹窗
    if (showDeleteConfirm) {
        MessageDialog(
            title = "确认删除",
            message = "确定要删除该老人信息吗？\n"
                    + "\n  姓名：${elder.name}"
                    + "\n  性别：${elder.gender.getDisplayName()}"
                    + "\n  年龄：${elder.age}",
            type = DialogType.WARNING,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.removeElder(elderId)
                onBackClick()
            },
            onDismiss = { showDeleteConfirm = false },
            showDismiss = true
        )
    }
}
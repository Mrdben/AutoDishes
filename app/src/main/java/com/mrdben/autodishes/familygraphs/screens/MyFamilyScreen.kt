package com.mrdben.autodishes.familygraphs.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.TextButton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight

import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.getDisplayName
import com.mrdben.autodishes.data.FamilyViewModel
import com.mrdben.autodishes.data.SortType
import com.mrdben.autodishes.data.SortOrder

/**
 * 我的家庭 - 老人资料列表页
 * 功能：展示所有老人（姓名/年龄/性别） + 底部添加按钮
 * 适配老年用户：大字号、宽间距、卡片式布局
 */
@Composable
fun MyFamilyScreen(
    // 点击添加按钮的跳转回调（后续对接添加老人页面）
    onAddElderClick: () -> Unit
) {
    // 获取家庭ViewModel（共享数据源）
    val familyViewModel: FamilyViewModel = viewModel()
    // 订阅家庭数据（生命周期安全）
    val currentFamily by familyViewModel.currentFamily.collectAsStateWithLifecycle()
    val currentSortState by familyViewModel.currentSortState.collectAsStateWithLifecycle()
    // 提取老人列表
    val elderList = currentFamily.elders
    // 对话框显隐状态
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 36.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 左侧三横菜单图标
            MenuIconButton(
                currentSortState = currentSortState,
                onModifyFamilyName = { showRenameDialog = true },
                onSortByName = { familyViewModel.toggleSortType(SortType.NAME) },
                onSortByAge = { familyViewModel.toggleSortType(SortType.AGE)},
                onSortByGender = { familyViewModel.toggleSortType(SortType.GENDER)}
            )

            // 居中标题（保留之前的样式优化）
            Text(
                text = currentFamily.familyName,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // 右侧占位（保持标题居中对称）
            //Box(modifier = Modifier.align(Alignment.CenterEnd))
        }

        // 老人列表区域
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 26.dp, vertical = 18.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            items(elderList) { elder ->
                ElderListItem(elder = elder)
            }
            item {
                AddElderCardButton(onClick = onAddElderClick)
            }
        }
    }
    FamilyRenameDialog(
        visible = showRenameDialog,
        originalName = currentFamily.familyName, // 传递原名称
        onDismiss = { showRenameDialog = false }, // 关闭对话框
        onConfirm = { newName ->
            // 调用 ViewModel 的更新方法（正确方式）
            familyViewModel.updateFamilyName(newName)
            showRenameDialog = false
        }
    )
}

/**
 * 老人列表项组件（复用）
 * 展示：姓名、年龄、性别
 */
@Composable
private fun ElderListItem(elder: Elder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 姓名（突出显示）
            Text(
                text = elder.name,
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium
            )
            // 年龄+性别
            Text(
                text = "年龄：${elder.age.toString().padStart(3, ' ')}岁   性别：${elder.gender.getDisplayName()}",
                fontSize = 18.sp,
                style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

//和老人项样式一致的添加按钮Card
@Composable
fun AddElderCardButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer(alpha = 0.5f),
        elevation = CardDefaults.cardElevation(),
        onClick = onClick // Card可点击，替代Button
    ) {
        // 🌟 修改3：Card内部居中显示大号加号
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // 调整Card高度，和老人项匹配
            contentAlignment = Alignment.Center
        ) {
            // 方案1：用文字加号（无需资源文件，推荐）
            Text(
                text = "[ + ]",
                fontSize = 48.sp, // 大号加号
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // 透明度设为50%
                // 可选优化：加轻微的字重，让加号更柔和
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                )
            )
        }
    }
}

@Composable
fun MenuIconButton(
    currentSortState: Pair<SortType, SortOrder>,
    onModifyFamilyName: () -> Unit,
    onSortByName: () -> Unit,
    onSortByAge: () -> Unit,
    onSortByGender: () -> Unit
) {
    // 控制下拉菜单的显示/隐藏
    var menuExpanded by remember { mutableStateOf(false) }
    val (currentSortType, currentSortOrder) = currentSortState
    val arrow = if (currentSortOrder == SortOrder.ASC) " ↑" else " ↓"
    var bold = FontWeight.Bold
    var showtext = ""

    // 左侧菜单图标按钮
    IconButton(
        onClick = { menuExpanded = !menuExpanded },
    ) {
        Icon(
            imageVector = Icons.Default.Menu, // 三横菜单图标（Material内置）
            contentDescription = "功能菜单",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // 柔和化图标颜色
        )

        // 下拉菜单（点击图标后显示）
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false } // 点击外部关闭菜单
        ) {
            // 1. 修改家庭名称
            DropdownMenuItem(
                text = { Text("修改家庭名称",
                    fontSize = 18.sp,
                )},
                onClick = {
                    onModifyFamilyName()
                    menuExpanded = false // 点击后关闭菜单
                }
            )
            // 2. 按姓名排序
            if (currentSortType == SortType.NAME) {
                bold = FontWeight.Bold
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = {  Text( "按姓名排序 $showtext",
                    fontSize = 18.sp,
                    fontWeight = bold
                )},
                onClick = {
                    onSortByName()
                    menuExpanded = false
                }
            )
            // 3. 按年龄排序
            if (currentSortType == SortType.AGE) {
                bold = FontWeight.Bold
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = { Text("按年龄排序 $showtext",
                    fontSize = 18.sp,
                    fontWeight = bold
                )},
                onClick = {
                    onSortByAge()
                    menuExpanded = false
                }
            )
            // 4. 按性别排序
            if (currentSortType == SortType.GENDER) {
                bold = FontWeight.Bold
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = { Text("按性别排序 $showtext",
                    fontSize = 18.sp,
                    fontWeight = bold
                )},
                onClick = {
                    onSortByGender()
                    menuExpanded = false
                }
            )
        }
    }
}

@Composable
fun FamilyRenameDialog(
    visible: Boolean, // 控制对话框显隐
    originalName: String, // 原家庭名称（用于回显）
    onDismiss: () -> Unit, // 关闭对话框的回调（取消/点击外部）
    onConfirm: (String) -> Unit // 确定修改的回调（传递新名称）
) {
    // 存储输入框内容（内部维护，主页面无需关心）
    var newFamilyName by remember { mutableStateOf(originalName) }

    // 仅当visible为true时显示对话框
    if (visible) {
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
}
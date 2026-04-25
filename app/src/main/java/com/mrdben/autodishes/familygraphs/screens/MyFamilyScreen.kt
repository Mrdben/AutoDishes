package com.mrdben.autodishes.familygraphs.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog

import com.mrdben.autodishes.MyApplication
import com.mrdben.autodishes.data.SortType
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.familygraphs.viewmodel.FamilyViewModel

/**
 * 我的家庭 - 老人资料列表页
 * 功能：展示所有老人（姓名/年龄/性别） + 底部添加按钮
 * 适配老年用户：大字号、宽间距、卡片式布局
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFamilyScreen(
    selectable: (Boolean) = false,
    onAddElderClick: () -> Unit,
    onBackClick: () -> Unit,
    onElderClick: (String) -> Unit,
    onViewDishList: () -> Unit,
    onViewOrderList: () -> Unit,
    onMakeMealClick: () -> Unit,
    onLogOff: () -> Unit
) {
    // 获取家庭ViewModel（共享数据源）
    Log.d("MyFamilyScreen","Before viewModel()")
    val familyViewModel: FamilyViewModel = viewModel(factory = MyApplication.Factory)
    Log.d("MyFamilyScreen","After viewModel()")
    // 订阅家庭数据（生命周期安全）
    val currentFamily by familyViewModel.currentFamily.collectAsStateWithLifecycle()
    val elders by familyViewModel.elders.collectAsStateWithLifecycle()
    val currentSortState by familyViewModel.currentSortState.collectAsStateWithLifecycle()
    val searchKeyword by familyViewModel.searchKeyword.collectAsStateWithLifecycle()

    var elderSection by remember { mutableStateOf(familyViewModel.getElderSelectionList())}
    var showRenameDialog by remember { mutableStateOf(false) }

    BackHandler {
        // 如果当前正在搜索 → 先清空搜索，不退出页面
        if (searchKeyword.isNotBlank()) {
            familyViewModel.clearSearch()
        } else {
            onBackClick()
        }
    }
    Scaffold(
        // 1. 顶部栏（可选）：页面标题/菜单
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text( if (selectable) "选择成员" else currentFamily.familyName) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (searchKeyword.isNotBlank()) {
                            familyViewModel.clearSearch()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    MenuIconButton(
                        currentSortState = currentSortState,
                        onLogOff = onLogOff,
                        onModifyFamilyName = { showRenameDialog = true },
                        onSortByName = { familyViewModel.toggleSortType(SortType.NAME) },
                        onSortByAge = { familyViewModel.toggleSortType(SortType.AGE)},
                        onSortByGender = { familyViewModel.toggleSortType(SortType.GENDER)},
                        onSearch = {searchText-> familyViewModel.search(searchText)}
                    )
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectable) {
                    Button(
                        onClick = {
                            familyViewModel.setElderSelectionList(elderSection)
                            onBackClick()
                            },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("确认选择", fontSize = 18.sp)
                    }
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("返回", fontSize = 18.sp)
                    }
                } else {
                    Button(
                        onClick = onMakeMealClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("》》》 去 做 饭 《《《", fontSize = 18.sp)
                    }
                    Button(
                        onClick = onViewDishList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看我的菜谱书", fontSize = 18.sp)
                    }
                    Button(
                        onClick = onViewOrderList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看我的用餐记录", fontSize = 18.sp)
                    }
                }
            }
        },
        // 2. 悬浮按钮（可选）：新增老人
        floatingActionButton = {
            FloatingActionButton(onClick = onAddElderClick) {
                Icon(Icons.Default.Add, contentDescription = "添加老人")
            }
        },
        // 3. 悬浮按钮位置（可选，默认右下角）
        floatingActionButtonPosition = FabPosition.End,
        // 4. 页面主内容区（必选）：你的 LazyColumn 列表
    ) { innerPadding ->
        //  关键：innerPadding 是 Scaffold 自动计算的内边距（避免内容被 topBar/bottomBar 遮挡）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 必须加！否则内容会顶到顶部栏
        ) {
            if (searchKeyword.isNotBlank()) {
                Text(
                    text = "搜索结果：$searchKeyword",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp
                )
            }
            // 你的主内容：标题、LazyColumn 列表等
            LazyColumn (
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 26.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )  {
                if (elders.isNotEmpty()) {
                    items(elders) { elder ->
                        if (selectable) {
                            val checked = elderSection.contains(elder.elderId)
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        elderSection = if (checked) {
                                            elderSection - elder.elderId
                                        } else {
                                            elderSection + elder.elderId
                                        }
                                    },
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null
                                )
                                ElderListItem(
                                    elder = elder,
                                )
                            }
                        } else {
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onElderClick(elder.elderId)}
                            ) {
                                ElderListItem(
                                    elder = elder,
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "暂无家人信息\n请点击右下方加号添加",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            style = TextStyle(lineHeight = 42.sp)
                        )
                    }
                }
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

@Composable
fun MenuIconButton(
    currentSortState: Pair<SortType, SortOrder>,
    onLogOff: () -> Unit,
    onModifyFamilyName: () -> Unit,
    onSortByName: () -> Unit,
    onSortByAge: () -> Unit,
    onSortByGender: () -> Unit,
    onSearch: (String) -> Unit
) {
    // 控制下拉菜单的显示/隐藏
    var menuExpanded by remember { mutableStateOf(false) }
    var showSearchDialog: Boolean by remember { mutableStateOf(false) }
    val (currentSortType, currentSortOrder) = currentSortState
    val arrow = if (currentSortOrder == SortOrder.ASC) " ↑" else " ↓"
    var bold: FontWeight
    var showText : String

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
    }
    // 下拉菜单（点击图标后显示）
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false } // 点击外部关闭菜单
    ) {
        // 0. 退出登录
        DropdownMenuItem(
            text = { Text("退出登录",
                fontSize = 18.sp,
            )},
            onClick = {
                onLogOff()
                menuExpanded = false // 点击后关闭菜单
            }
        )
        //HorizontalDivider()
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
        HorizontalDivider()
        // 2. 按姓名排序
        if (currentSortType == SortType.NAME) {
            bold = FontWeight.Bold
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = {  Text( "按姓名排序 $showText",
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
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = { Text("按年龄排序 $showText",
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
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = { Text("按性别排序 $showText",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onSortByGender()
                menuExpanded = false
            }
        )
        HorizontalDivider()
        // 5. 搜索
        DropdownMenuItem(
            text = { Text("搜索家庭成员",
                fontSize = 18.sp)},
            onClick = {
                menuExpanded = false
                showSearchDialog = true // 打开搜索弹窗
            }
        )
    }

    SearchElderDialog (
        showDialog = showSearchDialog,
        onDismiss = {showSearchDialog = false},
        onSearch = {searchText->
            showSearchDialog = false
            onSearch(searchText)
        }
    )
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

@Composable
fun SearchElderDialog (
    showDialog: Boolean,          // 控制显示/隐藏
    onDismiss: () -> Unit,        // 取消
    onSearch: (String) -> Unit    // 确定搜索
) {
    if (!showDialog) return

    var searchText by remember { mutableStateOf("刘") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "搜索家庭成员",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                // 搜索输入框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("请输入成员姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 18.sp)
                )

                // 取消 + 确定按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 取消
                    TextButton(onClick = onDismiss) {
                        Text("取消",fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 确定
                    TextButton(onClick = {
                        // 执行搜索
                        onSearch(searchText)
                        searchText = ""
                    }) {
                        Text("确定", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
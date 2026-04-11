package com.mrdben.autodishes.ordergraphs.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.data.MealType

import com.mrdben.autodishes.data.Order
import com.mrdben.autodishes.data.OrderViewModel
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.SortType
import com.mrdben.autodishes.data.getDisplayName


// ==========================
// 主页面：菜品列表页面
// ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderViewModel = viewModel(),
    onOrderClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    // 观察数据变化
    val orderList by viewModel.currentOrderList.collectAsStateWithLifecycle()
    val currentFilterType by viewModel.currentFilterType.collectAsStateWithLifecycle()
    val currentSortType by viewModel.currentSortType.collectAsStateWithLifecycle()
    val searchKeyword by viewModel.searchKeyword.collectAsStateWithLifecycle()
    BackHandler {
        // 如果当前正在搜索 → 先清空搜索，不退出页面
        if (searchKeyword.isNotBlank() || currentFilterType != MealType.UNKNOWN) {
            viewModel.clearFilterAndSearch()
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text( "我的用餐历史") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (searchKeyword.isNotBlank() || currentFilterType != MealType.UNKNOWN) {
                            viewModel.clearFilterAndSearch()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back to last page"
                        )
                    }
                },
                actions = {
                    MenuIconButton(
                        currentFilterType = currentFilterType,
                        currentSortType = currentSortType,
                        onLogOff = {/*Todo: log off*/},
                        onChangeSort = {sortType ->
                            viewModel.setSortType(sortType) },
                        onFilterByMeal = {mealType ->
                            viewModel.setFilter(mealType) },
                        onSearch = {searchText-> viewModel.search(searchText)}
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
                Button(onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("》》》 去 做 饭 《《《", fontSize = 18.sp)
                }
                Button(onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("返回", fontSize = 18.sp)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Add, contentDescription = "去做饭")
            }
        },
        // 3. 悬浮按钮位置（可选，默认右下角）
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 必须加 padding
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

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 26.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 列表渲染
                if (orderList.isNotEmpty()) {
                    items(orderList) { order ->
                        OrderItem(
                            order = order,
                            onClick = { onOrderClick(order.orderId) },
                            viewModel = viewModel
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "暂无用餐历史\n请点击右下方加号去做饭",
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
}

// ==========================
// 独立的 Item 组件：单个菜品显示
// 完全解耦、可复用
// ==========================
@Composable
fun OrderItem(
    order: Order,
    onClick: () -> Unit,
    viewModel: OrderViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onClick
    ) {
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            ){
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Text(
                    text = "时间：${order.formatDate()}",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 22.sp,
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                )
                Text(
                    text = order.mealType.getDisplayName(),
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 22.sp,
                    style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                )
            }
            Text(
                text = "用餐人：${ viewModel.getElderNameList(order.elderIds).joinToString("、") }",
                modifier = Modifier.padding(vertical = 4.dp),
                fontSize = 18.sp,
                style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            )
            Text(
                text = "餐品：${ viewModel.getDishNameList(order.dishIds).joinToString("、") }",
                modifier = Modifier.padding(vertical = 4.dp),
                fontSize = 18.sp,
                style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
fun MenuIconButton(
    currentFilterType: MealType,
    currentSortType: SortOrder,
    onLogOff: () -> Unit,
    onChangeSort:(SortOrder) -> Unit,
    onFilterByMeal: (MealType) -> Unit,
    onSearch: (String) -> Unit
) {
    // 控制下拉菜单的显示/隐藏
    var menuExpanded by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
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


        if (currentSortType == SortOrder.ASC) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = {  Text( "时间升序 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onChangeSort(SortOrder.ASC)
                menuExpanded = false
            }
        )
        if (currentSortType == SortOrder.DESC) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "时间降序 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onChangeSort(SortOrder.DESC)
                menuExpanded = false
            }
        )
        HorizontalDivider()
        if (currentFilterType == MealType.BREAKFAST) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "只看早餐 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onFilterByMeal(MealType.BREAKFAST)
                menuExpanded = false
            }
        )
        // 3. 只看午餐
        if (currentFilterType == MealType.LUNCH) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "只看午餐 $showtext",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onFilterByMeal(MealType.LUNCH)
                menuExpanded = false
            }
        )
        // 4. 只看晚餐
        if (currentFilterType == MealType.DINNER) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "只看晚餐 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onFilterByMeal(MealType.DINNER)
                menuExpanded = false
            }
        )
        // 4. 只看零食
        if (currentFilterType == MealType.SNACK) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "只看零食 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onFilterByMeal(MealType.SNACK)
                menuExpanded = false
            }
        )
        // 4. 看全部
        if (currentFilterType == MealType.UNKNOWN) {
            bold = FontWeight.Bold
            showtext = "√"
        } else {
            bold = FontWeight.Medium
            showtext = ""
        }
        DropdownMenuItem(
            text = {  Text( "看全部历史 ${showtext}",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onFilterByMeal(MealType.UNKNOWN)
                menuExpanded = false
            }
        )
        HorizontalDivider()
        // 5. 搜索
        DropdownMenuItem(
            text = { Text("搜索用餐历史",
                fontSize = 18.sp)},
            onClick = {
                menuExpanded = false
                showSearchDialog = true // 打开搜索弹窗
            }
        )
    }

    SearchDishDialog (
        showDialog = showSearchDialog,
        onDismiss = {showSearchDialog = false},
        onSearch = {searchText->
            showSearchDialog = false
            onSearch(searchText)
        }
    )
}

@Composable
fun SearchDishDialog (
    showDialog: Boolean,          // 控制显示/隐藏
    onDismiss: () -> Unit,        // 取消
    onSearch: (String) -> Unit    // 确定搜索
) {
    if (!showDialog) return

    var searchText by remember { mutableStateOf("奶奶") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "搜索菜品",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                // 搜索输入框
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("请输入菜品名称") },
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

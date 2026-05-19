package com.mrdben.autodishes.dishgraphs.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.MyApplication

import com.mrdben.autodishes.data.DishSortType
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.WorkState
import com.mrdben.autodishes.dishgraphs.viewmodel.DishViewModel


// ==========================
// 主页面：菜品列表页面
// ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishListScreen(
    selectable: (Boolean) = false,
    fromTemp: (Boolean) = false,
    onDishClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddDishClick: () -> Unit,
    onViewElderList: () -> Unit,
    onViewOrderList: () -> Unit,
    onMakeMealClick: () -> Unit,
    onUserProfile: () -> Unit,
    viewModel: DishViewModel = viewModel(factory = MyApplication.Factory),
) {
    // 观察数据变化
    viewModel.setUseTempDishes(fromTemp)
    val dishList by viewModel.currentDishList.collectAsStateWithLifecycle()
    val currentSortState by viewModel.currentSortState.collectAsStateWithLifecycle()
    val searchKeyword by viewModel.searchKeyword.collectAsStateWithLifecycle()
    var dishSelection by remember { mutableStateOf(viewModel.getDishSelectionList())}
    val workState by viewModel.workState.collectAsStateWithLifecycle()
    val aiWorkState by viewModel.aiWorkState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    val allDishIds = dishList.map{it.dishId}
    val isRefreshing = (if (fromTemp) aiWorkState else workState) == WorkState.Loading

    BackHandler {
        // 如果当前正在搜索 → 先清空搜索，不退出页面
        if (searchKeyword.isNotBlank()) {
            viewModel.clearSearch()
        } else {
            onBackClick()
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text( (if (selectable) "选择餐品" else "我的菜谱书") + " (${dishList.size})" )},
                navigationIcon = {
                    IconButton(onClick = {
                        if (searchKeyword.isNotBlank()) {
                            viewModel.clearSearch()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back to last page"
                        )
                    }
                },
                actions = {
                    MenuIconButton(
                        currentSortState = currentSortState,
                        onUserProfile = onUserProfile,
                        onSortByName = { viewModel.toggleSortType(DishSortType.NAME) },
                        onSortByTaste = { viewModel.toggleSortType(DishSortType.TASTE)},
                        onSortByType = { viewModel.toggleSortType(DishSortType.TYPE)},
                        onSortByCuisine = { viewModel.toggleSortType(DishSortType.CUISINE)},
                        onSortByFavorite = { viewModel.toggleSortType(DishSortType.FAVORITE)},
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
                if (selectable) {
                    Row (modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        Button(
                            onClick = {
                                dishSelection = (dishSelection + allDishIds).distinct()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("全选", fontSize = 18.sp)
                        }
                        Button(
                            onClick = {
                                val currentSelection = allDishIds.filter { it in dishSelection}
                                val revertSection = allDishIds.filter { it !in dishSelection}
                                dishSelection = (dishSelection - currentSelection.toSet() + revertSection.toSet()).distinct()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("反选", fontSize = 18.sp)
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.setDishSelectionList(dishSelection)
                            onMakeMealClick()
                        },
                        enabled = !isRefreshing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("确认选择", fontSize = 18.sp)
                    }
                    Button(
                        onClick = onMakeMealClick,
                        enabled = !isRefreshing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("返回", fontSize = 18.sp)
                    }
                } else {
                    Button(onClick = onMakeMealClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("》》》 去 做 饭 《《《", fontSize = 18.sp)
                    }
                    Button(onClick = onViewElderList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看我家庭", fontSize = 18.sp)
                    }
                    Button(onClick = onViewOrderList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看我的用餐记录", fontSize = 18.sp)
                    }
                }
            }
        },
        floatingActionButton = {
            if (!fromTemp) FloatingActionButton(onClick = onAddDishClick) {
                Icon(Icons.Default.Add, contentDescription = "添加菜品")
            }
        },
        // 3. 悬浮按钮位置（可选，默认右下角）
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        if (fromTemp) viewModel.refreshAIDishList(dishSelection)
                        else viewModel.refreshDishList()
                    },
                    state = pullToRefreshState,
                    threshold = 120.dp
                )
        ) {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = (pullToRefreshState.distanceFraction * 80).dp)
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
                if (isRefreshing) {
                    Text(
                        text = if (fromTemp) "您的AI营养师正在为您\n努力地搭配餐品\n\n请您耐心等待\n可能需要30秒钟的时间"
                        else "正在刷新菜品\n请稍后",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = TextStyle(lineHeight = 42.sp)
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 26.dp, vertical = 18.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 列表渲染
                    if (dishList.isNotEmpty()) {
                        items(dishList) { dish ->
                            if (selectable) {
                                val checked = dishSelection.contains(dish.dishId)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setDishSelectionList(dishSelection) // commit selection from screen to viewmodel
                                            onDishClick(dish.dishId)
                                        },
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            dishSelection = if (checked) {
                                                dishSelection - dish.dishId
                                            } else {
                                                dishSelection + dish.dishId
                                            }
                                        }
                                    )
                                    DishListItem(
                                        dish = dish,
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDishClick(dish.dishId) }
                                ) {
                                    DishListItem(
                                        dish = dish,
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = if (fromTemp) ("\n暂无菜品信息" + (if(!isRefreshing) "\n请下拉刷新" else ""))
                                       else "\n暂无菜品信息\n请点击右下方加号添加",
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
}

@Composable
fun MenuIconButton(
    currentSortState: Pair<DishSortType, SortOrder>,
    onUserProfile: () -> Unit,
    onSortByName: () -> Unit,
    onSortByTaste: () -> Unit,
    onSortByType: () -> Unit,
    onSortByCuisine: () -> Unit,
    onSortByFavorite: () -> Unit,
    onSearch: (String) -> Unit
) {
    // 控制下拉菜单的显示/隐藏
    var menuExpanded by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    val (currentSortType, currentSortOrder) = currentSortState
    val arrow = if (currentSortOrder == SortOrder.ASC) " ↑" else " ↓"
    var bold: FontWeight
    var showText: String

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
        // 0. 用户信息
        DropdownMenuItem(
            text = { Text("用户信息",
                fontSize = 18.sp,
            )},
            onClick = {
                onUserProfile()
                menuExpanded = false // 点击后关闭菜单
            }
        )
        HorizontalDivider()
        // 2. 按姓名排序
        if (currentSortType == DishSortType.NAME) {
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
        // 3. 按口味排序
        if (currentSortType == DishSortType.TASTE) {
            bold = FontWeight.Bold
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = {  Text( "按口味排序 $showText",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onSortByTaste()
                menuExpanded = false
            }
        )
        // 4. 按类型排序
        if (currentSortType == DishSortType.TYPE) {
            bold = FontWeight.Bold
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = { Text("按类型排序 $showText",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onSortByType()
                menuExpanded = false
            }
        )
        // 4. 按菜系排序
        if (currentSortType == DishSortType.CUISINE) {
            bold = FontWeight.Bold
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = { Text("按菜系排序 $showText",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onSortByCuisine()
                menuExpanded = false
            }
        )
        // 4. 按喜好排序
        if (currentSortType == DishSortType.FAVORITE) {
            bold = FontWeight.Bold
            showText = arrow
        } else {
            bold = FontWeight.Medium
            showText = ""
        }
        DropdownMenuItem(
            text = { Text("按喜好排序 $showText",
                fontSize = 18.sp,
                fontWeight = bold
            )},
            onClick = {
                onSortByFavorite()
                menuExpanded = false
            }
        )
        HorizontalDivider()
        // 5. 搜索
        DropdownMenuItem(
            text = { Text("搜索菜品",
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

    var searchText by remember { mutableStateOf("") }

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

package com.mrdben.autodishes.dishgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.data.Dish
import com.mrdben.autodishes.data.DishSortType
import com.mrdben.autodishes.data.DishViewModel
import com.mrdben.autodishes.data.SortOrder
import com.mrdben.autodishes.data.getDisplayName


// ==========================
// 主页面：菜品列表页面
// ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishListScreen(
    viewModel: DishViewModel = viewModel(),
    onDishClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddDishClick: () -> Unit
) {
    // 观察数据变化
    val dishList by viewModel.currentDishlist.collectAsStateWithLifecycle()
    val currentSortState by viewModel.currentSortState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text( "我的菜谱") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back to last page"
                        )
                    }
                },
                actions = {
                    MenuIconButton(
                        currentSortState = currentSortState,
                        onLogOff = {/*Todo: log off*/},
                        onSortByName = { viewModel.toggleSortType(DishSortType.NAME) },
                        onSortByTaste = { viewModel.toggleSortType(DishSortType.TASTE)},
                        onSortByType = { viewModel.toggleSortType(DishSortType.TYPE)},
                        onSortByCuisine = { viewModel.toggleSortType(DishSortType.CUISINE)},
                        onSortByFavorite = { viewModel.toggleSortType(DishSortType.FAVORITE)}
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDishClick) {
                Icon(Icons.Default.Add, contentDescription = "添加菜品")
            }
        },
        // 3. 悬浮按钮位置（可选，默认右下角）
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 26.dp, vertical = 18.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            // 列表渲染
            if (dishList.isNotEmpty()) {
                items(dishList) { dish ->
                    DishItem(
                        dish = dish,
                        onClick = { onDishClick(dish.dishId) }
                    )
                }
            } else {
                item {
                    Text(
                        text = "暂无菜品信息\n请点击右下方加号添加",
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

// ==========================
// 独立的 Item 组件：单个菜品显示
// 完全解耦、可复用
// ==========================
@Composable
fun DishItem(
    dish: Dish,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onClick
    ) {
        Column (modifier = Modifier.fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(Modifier.weight(1f)) {
                    // 菜名
                    Text(
                        text = dish.name,
                        fontSize = 23.sp,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Box(Modifier.weight(1f)) {
                    FavoriteStars(dish.favoriteLevel, 20)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(Modifier.weight(1f)) {
                    // 口味
                    Text(
                        text = "口味：${dish.taste}",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontSize = 18.sp,
                        style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    )
                }
                Box(Modifier.weight(1f)) {
                    // 菜系 + 类型
                    Text(
                        text = "${dish.cuisine.getDisplayName()} | ${dish.type.getDisplayName()}",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontSize = 18.sp,
                        style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
fun MenuIconButton(
    currentSortState: Pair<DishSortType, SortOrder>,
    onLogOff: () -> Unit,
    onSortByName: () -> Unit,
    onSortByTaste: () -> Unit,
    onSortByType: () -> Unit,
    onSortByCuisine: () -> Unit,
    onSortByFavorite: () -> Unit
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
            // 2. 按姓名排序
            if (currentSortType == DishSortType.NAME) {
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
            // 3. 按口味排序
            if (currentSortType == DishSortType.TASTE) {
                bold = FontWeight.Bold
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = {  Text( "按口味排序 $showtext",
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
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = { Text("按类型排序 $showtext",
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
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = { Text("按菜系排序 $showtext",
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
                showtext = arrow
            } else {
                bold = FontWeight.Medium
                showtext = ""
            }
            DropdownMenuItem(
                text = { Text("按喜好排序 $showtext",
                    fontSize = 18.sp,
                    fontWeight = bold
                )},
                onClick = {
                    onSortByFavorite()
                    menuExpanded = false
                }
            )
        }
    }
}

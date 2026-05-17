package com.mrdben.autodishes.dishgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.MyApplication

import com.mrdben.autodishes.dishgraphs.data.DEFAULT_DISH
import com.mrdben.autodishes.dishgraphs.data.getDisplayName
import com.mrdben.autodishes.dishgraphs.viewmodel.DishViewModel
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog
import com.mrdben.autodishes.utils.utcToLocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    dishId: String,
    readOnly: (Boolean) = false,
    onBackClick: () -> Unit,
    onEditDish: (String) -> Unit,
    viewModel: DishViewModel = viewModel(factory = MyApplication.Factory)
) {
    var dish by remember {mutableStateOf(DEFAULT_DISH)}
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var favoriteLevel by remember { mutableIntStateOf(dish.favoriteLevel) }

    LaunchedEffect(dishId) {
        val fetchedDish = viewModel.getDishById(dishId)
        dish = fetchedDish ?: DEFAULT_DISH
        favoriteLevel = dish.favoriteLevel
    }
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "菜品详情") },
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
                        onClick = { onEditDish(dishId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("编辑", fontSize = 18.sp)
                    }
                    Button(
                        onClick = {
                            // 只打开弹窗，不直接删除
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
            // 菜品信息卡片（核心展示区）
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
                        text = dish.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // 喜好程度
                    FavoriteStars(favoriteLevel, 28)
                    // 2. 口味
                    Text(
                        text = "口味：${dish.taste}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    // 3. 类型
                    Text(
                        text = "类型：${dish.type.getDisplayName()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    // 4. 菜系
                    Text(
                        text = "菜系：${dish.cuisine.getDisplayName()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    // 5. 原料
                    Text(
                        text = "原料：\n${dish.ingredients}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    // 5. 做法
                    Text(
                        text = "做法：\n${dish.recipe}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    // 6. 备注
                    Text(
                        text = "备注：\n${dish.comments}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "菜品编号：${dish.dishId}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "用户编号：${dish.userId}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "修改时间：${utcToLocalTime(dish.updatedAt)}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "创建时间：${utcToLocalTime(dish.createdAt)}",
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                        viewModel.like(dishId)
                        favoriteLevel = (favoriteLevel+1).fastCoerceAtMost(10)
                }){
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "赞",
                        tint = Color(0xFF9370DB),
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = {
                        viewModel.disLike(dishId)
                        favoriteLevel = (favoriteLevel-1).fastCoerceAtLeast(0)
                }){
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "踩",
                        tint = Color(0xFF9370DB),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
    // 弹窗
    if (showDeleteConfirm) {
        MessageDialog(
            title = "确认删除",
            message = "确定要删除该菜品信息吗？\n"
                    + "\n菜品名称：${dish.name}"
                    + "\n喜好度：${dish.favoriteLevel} /10"
                    + "\n口味：${dish.taste}"
                    + "\n菜系：${dish.cuisine.getDisplayName()}"
                    + "\n类型：${dish.type.getDisplayName()}",
            type = DialogType.WARNING,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.removeDishById(dishId)
                onBackClick()
            },
            onDismiss = {showDeleteConfirm = false},
            showDismiss = true
        )
    }
}
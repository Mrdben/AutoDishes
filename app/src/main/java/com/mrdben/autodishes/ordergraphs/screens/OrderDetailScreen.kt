package com.mrdben.autodishes.ordergraphs.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button

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

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.MyApplication

import com.mrdben.autodishes.ordergraphs.data.DEFAULT_ORDER
import com.mrdben.autodishes.ordergraphs.data.getDisplayName
import com.mrdben.autodishes.familygraphs.screens.ElderListItem
import com.mrdben.autodishes.dishgraphs.screens.DishListItem
import com.mrdben.autodishes.ordergraphs.viewmodel.OrderViewModel
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    onElderClick: (String) -> Unit,
    onDishClick: (String) -> Unit,
    onViewOrderList: () -> Unit,
    viewModel: OrderViewModel = viewModel(factory = MyApplication.Factory)
) {
    var order by remember {mutableStateOf(DEFAULT_ORDER)}
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        val fetchedOrder = viewModel.getOrderById(orderId)
        order = fetchedOrder ?: DEFAULT_ORDER
    }
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "用餐详情") },
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
                Button(onClick = onViewOrderList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("查看我的用餐记录", fontSize = 18.sp)
                }
                Button(onClick = {
                    // 只打开弹窗，不直接删除
                    showDeleteConfirm = true
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("删除这个记录", fontSize = 18.sp)
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
            verticalArrangement = Arrangement.Top
        ) {
            val textPadding = 8.dp
            val textSize = 20.sp
            Text(
                text = "用餐时间：${order.formatDate()}",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Text(
                text = "餐品类别：${order.mealType.getDisplayName()}",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Text(
                text = "用餐人员：",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Column(
                modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                order.elderIds.forEach { elderId ->
                    val elder = viewModel.getElderById(elderId)
                    if (elder != null) {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onElderClick(elderId)}
                        ) {
                            ElderListItem(elder)
                        }
                    }
                }
            }

            Text(
                text = "餐品明细：",
                modifier = Modifier.padding(textPadding).padding(top = textPadding*2),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                order.dishIds.forEach { dishId ->
                    val dish = viewModel.getDishById(dishId)
                    if (dish != null) {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDishClick(dishId)}
                        ) {
                            DishListItem(dish)
                        }
                    }
                }
            }
            // 8. 老人ID（可选展示，便于调试/管理）
            Text(
                text = "编号：${order.orderId}",
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Right
            )
        }
    }
    // 弹窗
    if (showDeleteConfirm) {
        MessageDialog(
            title = "确认删除",
            message = "确定要删除该用餐信息吗？\n"
                    + "\n用餐时间：${order.formatDate()}"
                    + "\n类别：${order.mealType.getDisplayName()}"
                    + "\n用餐人：${viewModel.getElderNameList(order.elderIds).joinToString("、")}"
                    + "\n餐品：${viewModel.getDishNameList(order.dishIds).joinToString("、")}",
            type = DialogType.WARNING,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.removeOrderById(orderId)
                onBackClick()
            },
            onDismiss = { showDeleteConfirm = false },
            showDismiss = true
        )
    }
}
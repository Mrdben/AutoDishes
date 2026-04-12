package com.mrdben.autodishes.ordergraphs.screens

import android.util.Log
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

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.viewmodel.compose.viewModel

import com.mrdben.autodishes.data.DEFAULT_ORDER
import com.mrdben.autodishes.data.Order
import com.mrdben.autodishes.data.OrderRepository
import com.mrdben.autodishes.data.getDisplayName
import com.mrdben.autodishes.familygraphs.screens.ElderListItem
import com.mrdben.autodishes.dishgraphs.screens.DishListItem
import com.mrdben.autodishes.ordergraphs.viewmodel.MakeMealViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeMealScreen(
    onBackClick: () -> Unit,
    viewModel: MakeMealViewModel = viewModel(),
    onElderClick: (String) -> Unit,
    onDishClick: (String) -> Unit,
    onConfirmOrder: (String) -> Unit
) {
    val newOrder by viewModel.newOrder.collectAsStateWithLifecycle()
    viewModel.createNewOrder()

    if (newOrder == null) return
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "一起来做饭") },
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
                Button(onClick = {
                    val orderId = newOrder!!.orderId
                    viewModel.confirmOrder()
                    onConfirmOrder(orderId)
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确定选择", fontSize = 18.sp)
                }
                Button(onClick = onBackClick,
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
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            val textPadding = 8.dp
            val textSize = 20.sp
            Text(
                text = "用餐时间：${newOrder!!.formatDate()}",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Text(
                text = "餐品类别：${newOrder!!.mealType.getDisplayName()}",
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
                newOrder!!.elderIds.forEach { elderId ->
                    val elder = viewModel.getElderById(elderId)
                    if (elder != null) {
                        ElderListItem(
                            elder = elder,
                            onItemClick = { onElderClick(elderId) },
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(),
                    onClick = { }
                ) {
                    Text(
                        text = "》》 选择用餐人员 《《",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = "餐品明细：",
                modifier = Modifier
                    .padding(textPadding)
                    .padding(top = textPadding * 2),
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
                newOrder!!.dishIds.forEach { dishId ->
                    val dish = viewModel.getDishById(dishId)
                    if (dish != null) {
                        DishListItem(
                            dish = dish,
                            onItemClick = { onDishClick(dishId) },
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(),
                    onClick = { }
                ) {
                    Text(
                        text = "》》 选择菜品 《《",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}

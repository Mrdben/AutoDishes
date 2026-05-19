package com.mrdben.autodishes.ordergraphs.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.MyApplication

import com.mrdben.autodishes.ordergraphs.data.MealType
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.getDisplayName
import com.mrdben.autodishes.familygraphs.screens.ElderListItem
import com.mrdben.autodishes.dishgraphs.screens.DishListItem
import com.mrdben.autodishes.ordergraphs.viewmodel.MakeMealViewModel
import com.mrdben.autodishes.utils.DialogType
import com.mrdben.autodishes.utils.MessageDialog
import com.mrdben.autodishes.utils.utcToLocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeMealScreen(
    orderId: String = "",
    onBackClick: () -> Unit,
    onElderClick: (String) -> Unit,
    onDishClick: (String) -> Unit,
    onConfirmOrder: () -> Unit,
    onSelectElder: () -> Unit,
    onSelectDish: () -> Unit,
    onSelectDishFromTemp: () -> Unit,
    viewModel: MakeMealViewModel = viewModel(factory = MyApplication.Factory)
) {
    val newOrder by viewModel.newOrder.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf("") }
    var showWarningDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.initialize(orderId)
    }
    // 脚手架布局（带顶部栏）
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "一起来做饭") },
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
                Button(onClick = {
                    confirmOrder(
                        newOrder = newOrder,
                        viewModel = viewModel,
                        onConfirmOrder = onConfirmOrder,
                        showError = { msg ->
                            errorMessage = msg
                            showWarningDialog = true
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确定选择", fontSize = 18.sp)
                }
                Button(onClick = onConfirmOrder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("返回列表", fontSize = 18.sp)
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
                text = "用餐时间：${utcToLocalTime(newOrder.mealTime)}",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            Text(
                text = "餐品类别：",
                modifier = Modifier.padding(textPadding),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize
            )
            MealTypeSelectionMenu(newOrder = newOrder,
                viewModel = viewModel,
                textSize = textSize)
            Text(
                text = "用餐人员（${newOrder.elderIds.size}人）：",
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
                viewModel.getElderList().forEach { elder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onElderClick(elder.elderId) }
                    ) {
                        ElderListItem(elder)
                    }
                }
                Button(
                    onClick = onSelectElder,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = ButtonDefaults.elevatedButtonElevation(), // 保持阴影
                    shape = MaterialTheme.shapes.medium // 和 Card 一样的圆角
                ) {
                    Text(
                        text = "》》 选择用餐人员 《《",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = "来自菜谱书 （${newOrder.dishIds.size}道菜）：",
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
                viewModel.getSelectedDishList().forEach { dish ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDishClick(dish.dishId) }
                    ) {
                        DishListItem(dish)
                    }
                }
                Button(
                    onClick = onSelectDish,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = ButtonDefaults.elevatedButtonElevation(), // 保持阴影
                    shape = MaterialTheme.shapes.medium // 和 Card 一样的圆角
                ) {
                    Text(
                        text = "》》 从菜谱书选择 《《",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            val tempDishes = viewModel.getSelectedTempDishList()
            Text(
                text = "来自AI推荐 （${tempDishes.size}道菜）：",
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
                tempDishes.forEach { dish ->
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDishClick(dish.dishId)}
                    ) {
                        DishListItem(dish)
                    }
                }
                Button(
                    onClick = {
                        if (newOrder.elderIds.isEmpty()) {
                            errorMessage = "请选择至少一个用餐人员。"
                            showWarningDialog = true
                        } else {
                            viewModel.autoDishes()
                            onSelectDishFromTemp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // 金色
                        contentColor = Color.Black         // 文字黑色（清晰）
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(), // 保持阴影
                    shape = MaterialTheme.shapes.medium // 和 Card 一样的圆角
                ) {
                    Text(
                        text = "》》 AI 推荐 《《",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    if (showWarningDialog) {
        MessageDialog(
            title = "请补充信息",
            message = errorMessage,
            type = DialogType.INFO,
            onConfirm = { showWarningDialog = false },
            onDismiss = {},
            showDismiss = false
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTypeSelectionMenu (
    newOrder: (Order?),
    viewModel: (MakeMealViewModel),
    textSize: (TextUnit) = 20.sp
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        // 控制菜单展开
        expanded = expanded,
        // 点击文本框时，自动切换展开状态
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = newOrder?.mealType?.getDisplayName() ?: "请选择餐品类型",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            textStyle = TextStyle(fontSize = textSize),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))  // 阴影+圆角
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
        ) {
            MealType.entries.toList().forEach { type ->
                if (type != MealType.UNKNOWN) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                type.getDisplayName(),
                                fontSize = textSize
                            )
                        },
                        onClick = {
                            viewModel.setMealType(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun confirmOrder (
    newOrder: (Order),
    viewModel: (MakeMealViewModel),
    onConfirmOrder: () -> Unit,
    showError: (String) -> Unit
) {
    if (newOrder.elderIds.isNotEmpty()
        && newOrder.dishIds.isNotEmpty()
        && newOrder.mealType != MealType.UNKNOWN
    ) {
        viewModel.confirmOrder()
        onConfirmOrder()
    } else {
        val msg = "请选择:\n" +
                (if (newOrder.mealType == MealType.UNKNOWN) "  ->餐品类别\n" else "") +
                (if (newOrder.elderIds.isEmpty()) "  ->至少一个用餐人员\n" else "") +
                (if (newOrder.dishIds.isEmpty()) "  ->至少一个餐品\n" else "")
        showError(msg)
    }
}
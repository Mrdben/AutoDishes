package com.mrdben.autodishes.dishgraphs.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.data.CuisineType
import com.mrdben.autodishes.data.DEFAULT_DISH
import com.mrdben.autodishes.data.Dish
import com.mrdben.autodishes.data.DishRepository
import com.mrdben.autodishes.data.DishType
import com.mrdben.autodishes.data.DishViewModel
import com.mrdben.autodishes.data.getDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditScreen(
    dishId: String,
    onCancel: () -> Unit,
    viewModel: DishViewModel = viewModel()
) {
    var dish by remember {mutableStateOf<Dish?>(null)}
    val focusManager = LocalFocusManager.current
    LaunchedEffect(dishId) {
        dish = if (dishId.isBlank()) {
            DEFAULT_DISH
        } else {
            DishRepository.instance.getDishById(dishId) ?: DEFAULT_DISH
        }
    }

    // 3. 等待数据加载完成（必须）
    val currentDish = dish ?: return // 空的时候不渲染界面

    // ======================= 基础信息 =======================
    var name by remember { mutableStateOf(currentDish.name) }
    var taste by remember { mutableStateOf(currentDish.taste) }
    var type by remember { mutableStateOf(currentDish.type) }
    var cuisine by remember { mutableStateOf(currentDish.cuisine) }
    var ingredients by remember { mutableStateOf(currentDish.ingredients) }
    var recipe by remember { mutableStateOf(currentDish.recipe) }
    var favoriteLevel by remember { mutableStateOf(currentDish.favoriteLevel) }


    // ======================= 页面结构 =======================
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (dishId.isBlank()) "新增菜品信息" else "编辑菜品信息") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
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
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val newDish = Dish(
                            dishId = dishId.takeIf { it.isNotBlank() } ?: "dish_${System.currentTimeMillis()}",
                            name = name,
                            taste = taste,
                            type = type,
                            cuisine = cuisine,
                            ingredients = ingredients,
                            recipe = recipe,
                            favoriteLevel = favoriteLevel
                        )
                        if (dishId.isBlank()) {
                            viewModel.addDish(newDish)
                        } else {
                            viewModel.updateDish(newDish)
                        }
                        onCancel()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认保存", fontSize = 18.sp)
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消", fontSize = 18.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ========== 基础信息 ==========
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("菜品名称", fontSize = 18.sp)},
                textStyle = TextStyle(fontSize = 20.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 16.dp)
            )
            OutlinedTextField(
                value = taste,
                onValueChange = { taste = it },
                label = { Text("口味", fontSize = 18.sp) },
                textStyle = TextStyle(fontSize = 20.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("菜品类型:", style = MaterialTheme.typography.titleLarge)
            DishTypeRadioGroup(
                selectedType = type,
                onTypeSelected = { type = it }
            )
            Text("所属菜系:", style = MaterialTheme.typography.titleLarge)
            CuisineTypeRadioGroup(
                selectedCuisine = cuisine,
                onCuisineSelected = { cuisine = it }
            )
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("原料", fontSize = 18.sp) },
                textStyle = TextStyle(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = recipe,
                onValueChange = { recipe = it },
                label = { Text("做法", fontSize = 18.sp) },
                textStyle = TextStyle(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
            )

        }
    }
}

@Composable
fun <T : Enum<T>> EnumRadioGroup(
    selected: T,
    types: List<T>,
    getDisplayName: (T) -> String,
    onSelected: (T) -> Unit
) {
    val mid = (types.size + 1) / 2
    val left = types.subList(0, mid)
    val right = types.subList(mid, types.size)
    Card {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(Modifier.weight(1f)) {
                ColumnBoxes(
                    types = left,
                    selected = selected,
                    getDisplayName = getDisplayName,
                    onSelected = onSelected
                )
            }
            Box(Modifier.weight(1f)) {
                ColumnBoxes(
                    types = right,
                    selected = selected,
                    getDisplayName = getDisplayName,
                    onSelected = onSelected
                )
            }
        }
    }
}

@Composable
fun <T : Enum<T>> ColumnBoxes(
    selected: T,
    types: List<T>,
    getDisplayName: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        types.forEach { type ->
            val isSelected = selected == type
            RadioButtonRow(
                text = getDisplayName(type),
                selected = isSelected,
                onClick = {
                    onSelected(type)
                }
            )
        }
    }
}
@Composable
fun RadioButtonRow (
    text : String,
    selected : Boolean,
    onClick: () -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(
            selected = selected,
            onClick  = onClick
        )
        Text(
            text = text,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
@Composable
fun DishTypeRadioGroup(
    selectedType: DishType,
    onTypeSelected: (DishType) -> Unit
) {
    EnumRadioGroup(
        selected = selectedType,
        types = DishType.entries.toList(),
        getDisplayName = { it.getDisplayName() },
        onSelected = onTypeSelected
    )
}

@Composable
fun CuisineTypeRadioGroup(
    selectedCuisine: CuisineType,
    onCuisineSelected: (CuisineType) -> Unit
) {
    EnumRadioGroup(
        selected = selectedCuisine,
        types = CuisineType.entries.toList(),
        getDisplayName = { it.getDisplayName() },
        onSelected = onCuisineSelected
    )
}
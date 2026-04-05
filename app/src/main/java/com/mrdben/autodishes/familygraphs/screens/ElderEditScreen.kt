package com.mrdben.autodishes.familygraphs.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrdben.autodishes.data.DEFAULT_ELDER
import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.FamilyRepository
import com.mrdben.autodishes.data.FamilyViewModel
import com.mrdben.autodishes.data.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderEditScreen(
    elderId: String,
    onCancel: () -> Unit,
    viewModel: FamilyViewModel = viewModel()
) {
    var elder by remember {mutableStateOf<Elder?>(null)}
    val focusManager = LocalFocusManager.current
    LaunchedEffect(elderId) {
        elder = if (elderId.isBlank()) {
            DEFAULT_ELDER
        } else {
            FamilyRepository.instance.getElderById(elderId) ?: DEFAULT_ELDER
        }
    }

    // 3. 等待数据加载完成（必须）
    val currentElder = elder ?: return // 空的时候不渲染界面

    // ======================= 基础信息 =======================
    var name by remember { mutableStateOf(currentElder.name) }
    var age by remember { mutableStateOf(currentElder.age.toString()) }
    var gender by remember { mutableStateOf(currentElder.gender) }

    // ======================= 选项组（预设多选） =======================
    // 过敏源
    val allergenOptions = listOf("海鲜", "牛奶","羊肉", "鸡蛋", "花生/坚果", "芒果","菠萝","榴莲")
    var selectedAllergens by remember { mutableStateOf(currentElder.allergens.intersect(allergenOptions).toList()) }
    var otherAllergens by remember { mutableStateOf(currentElder.allergens - allergenOptions)}

    // 疾病
    val diseaseOptions = listOf("高血压", "糖尿病", "心脏病", "高血脂", "关节炎", "骨质疏松","通风","脂肪肝")
    var selectedDiseases by remember { mutableStateOf(currentElder.diseases.intersect(diseaseOptions).toList()) }
    var otherDiseases by remember { mutableStateOf(currentElder.diseases - diseaseOptions)}
    // 忌口
    val dietOptions = listOf("辛辣", "生冷","下水/内脏","香菜", "油腻", "过甜", "过咸", "硬质食物")
    var selectedDiet by remember { mutableStateOf(currentElder.dietaryRestrictions.intersect(dietOptions).toList()) }
    var otherDiet by remember { mutableStateOf(currentElder.dietaryRestrictions - dietOptions)}
    // 口味偏好
    val tasteOptions = listOf("清淡", "偏甜", "偏咸", "偏酸", "软烂", "流质","素食","高蛋白/高营养")
    var selectedTaste by remember { mutableStateOf(currentElder.tastePreferences.intersect(tasteOptions).toList()) }
    var otherTaste by remember { mutableStateOf(currentElder.tastePreferences - tasteOptions)}
    // ======================= 页面结构 =======================
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (elderId.isBlank()) "新增家庭成员" else "编辑成员信息") },
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
                        val newElder = Elder(
                            elderId = elderId.takeIf { it.isNotBlank() } ?: "new_${System.currentTimeMillis()}",
                            name = name,
                            age = age.toIntOrNull() ?: 0,
                            gender = gender,
                            allergens = selectedAllergens + otherAllergens,
                            diseases = selectedDiseases + otherDiseases,
                            dietaryRestrictions = selectedDiet + otherDiet,
                            tastePreferences = selectedTaste + otherTaste
                        )
                        if (elderId.isBlank()) {
                            viewModel.addElder(newElder)
                        } else {
                            viewModel.updateElder(newElder)
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
            Text("基础信息", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名", fontSize = 18.sp)},
                textStyle = TextStyle(fontSize = 20.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { newAge ->
                    // 只允许输入数字
                    if (newAge.all { char -> char.isDigit() }) {
                        age = newAge
                    }
                },
                label = { Text("年龄", fontSize = 18.sp) },
                textStyle = TextStyle(fontSize = 20.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("性别：  ", fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
                RadioButton(
                    selected = gender == Gender.MALE,
                    onClick = { gender = Gender.MALE }
                )
                Text("男", fontSize = 18.sp)
                RadioButton(
                    selected = gender == Gender.FEMALE,
                    onClick = { gender = Gender.FEMALE }
                )
                Text("女", fontSize = 18.sp)
            }

            // ========== 过敏源 ==========
            Text("过敏源：", style = MaterialTheme.typography.titleLarge)
            Card {
                MultiOptionBoxes(
                    options = allergenOptions,
                    selected = selectedAllergens,
                    onSelect = { selectedAllergens = it }
                )
                OtherBox(
                    options = otherAllergens,
                    onSelect = { otherAllergens = it }
                )
            }
            // ========== 疾病 ==========
            Text("疾病:", style = MaterialTheme.typography.titleLarge)
            Card {
                MultiOptionBoxes(
                    options = diseaseOptions,
                    selected = selectedDiseases,
                    onSelect = { selectedDiseases = it }
                )
                OtherBox(
                    options = otherDiseases,
                    onSelect = { otherDiseases = it }
                )
            }
            // ========== 忌口 ==========
            Text("饮食忌口:", style = MaterialTheme.typography.titleLarge)
            Card {
                MultiOptionBoxes(
                    options = dietOptions,
                    selected = selectedDiet,
                    onSelect = { selectedDiet = it }
                )
                OtherBox(
                    options = otherDiet,
                    onSelect = { otherDiet = it }
                )
            }
            // ========== 口味偏好 ==========
            Text("口味偏好:", style = MaterialTheme.typography.titleLarge)
            Card {
                MultiOptionBoxes(
                    options = tasteOptions,
                    selected = selectedTaste,
                    onSelect = { selectedTaste = it }
                )
                OtherBox(
                    options = otherTaste,
                    onSelect = { otherTaste = it }
                )
            }
        }
    }
}

// ======================= 多选标签组件（复用） =======================
@Composable
fun MultiOptionBoxes(
    options: List<String>,
    selected: List<String>,
    onSelect: (List<String>) -> Unit
) {
    val mid = (options.size + 1) / 2
    val left = options.subList(0, mid)
    val right = options.subList(mid, options.size)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(Modifier.weight(1f)) {
            ColumnBoxes(
                options = left,
                selected = selected,
                onSelect = onSelect
            )
        }
        Box(Modifier.weight(1f)) {
            ColumnBoxes(
                options = right,
                selected = selected,
                onSelect = onSelect
            )
        }
    }
}
@Composable
fun ColumnBoxes(
    options: List<String>,
    selected: List<String>,
    onSelect: (List<String>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected.contains(option)
            CheckboxRow(
                option,
                checked = isSelected,
                onCheckedChange ={
                    val newList = if (isSelected) selected - option else selected + option
                    onSelect(newList)
                }
            )
        }
    }
}
@Composable
fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange?.invoke(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
@Composable
fun OtherBox(
    options: List<String>,
    onSelect: (List<String>) -> Unit,
) {
    // 内部维护输入框的原始文本（允许随便输入符号）
    val inputText = remember { mutableStateOf(options.joinToString(", ")) }
    val isSelected = options.isNotEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(end = 20.dp).padding(bottom = 10.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = {
                if (isSelected) {
                    onSelect(emptyList())
                    inputText.value = ""
                } else {
                    inputText.value = "" // 清空输入框
                    onSelect(listOf(""))
                }
            }
        )

        TextField(
            value = inputText.value,
            onValueChange = { newText ->
                inputText.value = newText
            },
            modifier = Modifier
                .fillMaxWidth()
                // 失去焦点时 才执行切割格式化
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) { // 失去焦点
                        val finalList = othersplit(inputText.value)
                        // 更新列表
                        onSelect(finalList)
                        // 同时把输入框格式化成干净的文字
                        inputText.value = finalList.joinToString(", ")
                    }
                },
            enabled = isSelected,
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.75f),   // 输入时背景
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f) // 未输入时背景
            //disabledContainerColor = Color.White.copy(alpha = 0.2f),
        )
        )
    }
}

fun othersplit(
    longstring : String
): List<String> {
    return longstring.split("[ ,，、;]".toRegex()) // 按各种分隔符切
        .filter { it.isNotBlank() }  // 去空
}
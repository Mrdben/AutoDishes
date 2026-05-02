package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import com.mrdben.autodishes.utils.ValidationResult
import com.mrdben.autodishes.utils.ValidationUtils

@Composable
fun PasswordSettingSection(
    onValidPasswordChange: (String) -> Unit,
) {
    // 组件内部维护状态（对外仅暴露回调，解耦外部逻辑）
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(ValidationResult()) }
    isPasswordValid = ValidationUtils.validatePassword(password)

    val validPassword = remember(password, confirmPassword, showPassword) {
        when {
            // 1. 显示密码：直接返回password（非空则返回，空则返回空）
            showPassword -> password
            // 2. 不显示密码：校验password和confirmPassword一致且非空，一致则返回，否则返回空
            password == confirmPassword && password.isNotBlank() -> password
            // 3. 其他情况（不一致/空）：返回空
            else -> ""
        }
    }
    LaunchedEffect(validPassword) {
        onValidPasswordChange(validPassword)
    }

    val isConfirmPasswordError = remember(password, confirmPassword, showPassword) {
        // 仅当不显示密码、确认密码非空、且和密码不一致时，标记为错误
        !showPassword && confirmPassword.isNotBlank() && password != confirmPassword
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("请输入新密码", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            textStyle = TextStyle(fontSize = 18.sp),
            isError = !isPasswordValid.isValid,
            supportingText = {
                Text(text = isPasswordValid.errorMessage ?: "",
                    fontSize = 12.sp)
            }
        )

        Spacer(modifier = Modifier.size(18.dp))

        // 2. 确认密码输入框（仅当不显示密码时展示）
        if (!showPassword) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("请确认新密码", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
                ),
                textStyle = TextStyle(fontSize = 18.sp),
                isError = isConfirmPasswordError
            )

            Spacer(modifier = Modifier.size(10.dp))
        }

        // 3. 显示密码复选框
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showPassword,
                    onCheckedChange = { isChecked ->
                        if (isChecked != null) {
                            showPassword = isChecked
                            // 显示密码时，清空确认密码（符合交互逻辑）
                            if (isChecked) confirmPassword = ""
                        }
                    }
                )
                Text(
                    text = "显示密码",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
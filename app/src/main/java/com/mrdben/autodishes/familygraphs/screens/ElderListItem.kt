package com.mrdben.autodishes.familygraphs.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrdben.autodishes.data.Elder
import com.mrdben.autodishes.data.getDisplayName

@Composable
fun ElderListItem(
    elder: Elder,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 姓名（突出显示）
            Text(
                text = elder.name,
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium
            )
            // 年龄+性别
            Text(
                text = "年龄：${elder.age.toString().padStart(3, ' ')}岁   性别：${elder.gender.getDisplayName()}",
                fontSize = 18.sp,
                style = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
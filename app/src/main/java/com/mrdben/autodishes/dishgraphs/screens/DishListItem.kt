package com.mrdben.autodishes.dishgraphs.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.getDisplayName

@Composable
fun DishListItem(
    dish: Dish,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        onClick = onItemClick
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
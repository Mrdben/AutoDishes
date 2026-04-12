package com.mrdben.autodishes.dishgraphs.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteStars (
    level : Int,
    size : Int
) {
    Row {
        for (i in 0 until 5) {
            val starProgress = level - (i * 2)
            val icon = when {
                starProgress >= 2 -> Icons.Filled.Star       // 满星
                starProgress == 1 -> Icons.AutoMirrored.Filled.StarHalf   // 半星
                else -> Icons.Outlined.StarOutline           // 空星
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.padding(2.dp)
                    .size(size.dp)
            )
        }
    }
}
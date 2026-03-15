package com.mrdben.autodishes.authgraphs.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

import androidx.navigation.NavController

import kotlinx.coroutines.delay
import coil.compose.AsyncImage

import com.mrdben.autodishes.R

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    delayMillis: Long = 1000
) {
    // 全屏Box容器：白色背景，居中展示LOGO
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = R.drawable.autodishes_logo1_512,
                contentDescription = "splash logo",
                modifier = Modifier.fillMaxWidth(0.4f),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
            )
            Text(
                text = "~ by ~\nBenjamin Yang",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
    LaunchedEffect(key1 = Unit) {
        delay(delayMillis)
        onNavigateToLogin()
    }
}

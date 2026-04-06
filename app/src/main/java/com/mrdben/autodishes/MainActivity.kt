package com.mrdben.autodishes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mrdben.autodishes.ui.theme.AutoDishesTheme
import com.mrdben.autodishes.authgraphs.navigation.SetupAuthNavGraph
import com.mrdben.autodishes.familygraphs.navigation.SetupFamilyNavGraph
import com.mrdben.autodishes.dishgraphs.navigation.SetupDishNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoDishesTheme {
                SetupFamilyNavGraph()
            }
        }
    }
}

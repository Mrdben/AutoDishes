package com.mrdben.autodishes

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.ui.theme.AutoDishesTheme
import com.mrdben.autodishes.navigation.MainNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            AutoDishesTheme {
                // 只需要这一行
                MainNavGraph()
            }
        }
    }


}
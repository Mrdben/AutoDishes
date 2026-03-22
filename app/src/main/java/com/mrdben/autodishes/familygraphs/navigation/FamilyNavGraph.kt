package com.mrdben.autodishes.familygraphs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen


// 定义路由常量
sealed class Screen(val route: String) {
    object MyFamily : Screen("my_family_sceen")
}

@Composable
fun SetupFamilyNavGraph() {
    val myFamilyNavController = rememberNavController()

    NavHost(
        navController = myFamilyNavController,
        startDestination = Screen.MyFamily.route
    ) {
        composable(route = Screen.MyFamily.route) {
            MyFamilyScreen(
                onAddElderClick = {}
            )
        }

    }
}
package com.mrdben.autodishes.familygraphs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen
import com.mrdben.autodishes.familygraphs.screens.ElderDetailScreen

// 定义路由常量
sealed class Screen(val route: String) {
    object MyFamily : Screen("my_family_sceen")
    object ElderDetail : Screen("elder_details/{elderId}")
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
                onAddElderClick = {},
                onBackClick = { myFamilyNavController.popBackStack() },
                onElderClick = {elderId ->
                    myFamilyNavController.navigate("elder_details/$elderId")}
            )
        }
        composable(Screen.ElderDetail.route) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") ?: "debug_elder_001"
            ElderDetailScreen(
                elderId = elderId, // 改为传递解析后的 elderId
                onBackClick = { myFamilyNavController.popBackStack() }
            )
        }

    }
}
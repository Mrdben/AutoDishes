package com.mrdben.autodishes.familygraphs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen
import com.mrdben.autodishes.familygraphs.screens.ElderDetailScreen
import com.mrdben.autodishes.familygraphs.screens.ElderEditScreen

// 定义路由常量
sealed class Screen(val route: String) {
    object MyFamily : Screen("my_family_sceen")
    object ElderDetail : Screen("elder_details/{elderId}")
    object EditElder : Screen("edit_elder/{elderId}")
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
                onAddElderClick = {
                    myFamilyNavController.navigate("edit_elder/ ")
                },
                onBackClick = {
                    if (myFamilyNavController.previousBackStackEntry != null) {
                        myFamilyNavController.popBackStack()
                    }
                },
                onElderClick = {elderId ->
                    myFamilyNavController.navigate("elder_details/$elderId")
                }
            )
        }
        composable(Screen.ElderDetail.route) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") ?: "debug_elder_001"
            ElderDetailScreen(
                elderId = elderId, // 改为传递解析后的 elderId
                onBackClick = {
                    if (myFamilyNavController.previousBackStackEntry != null) {
                        myFamilyNavController.popBackStack()
                    }
                },
                onEditElder = {elderId ->
                    myFamilyNavController.navigate("edit_elder/$elderId")
                }
            )
        }
        composable(Screen.EditElder.route) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") ?: ""
            ElderEditScreen(
                elderId = elderId, // 改为传递解析后的 elderId
                onCancel = { myFamilyNavController.popBackStack() }
            )
        }
    }
}
package com.mrdben.autodishes.dishgraphs.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.dishgraphs.screens.DishListScreen
import com.mrdben.autodishes.dishgraphs.screens.DishDetailScreen
import com.mrdben.autodishes.dishgraphs.screens.DishEditScreen


// 定义路由常量
sealed class Screen(val route: String) {
    object DishListScreen : Screen("dish_list_screen")
    object DishDetailScreen : Screen("dish_detail_screen/{dishId}")
    object DishEditScreen : Screen("dish_edit_screen/{dishId}")
}

@Composable
fun SetupDishNavGraph() {
    val myDishNavController = rememberNavController()

    NavHost(
        navController = myDishNavController,
        startDestination = Screen.DishListScreen.route
    ) {
        composable(route = Screen.DishListScreen.route) {
            DishListScreen(
                onDishClick = {dishId ->
                    myDishNavController.navigate("dish_detail_screen/$dishId")},
                onBackClick= {
                    if (myDishNavController.previousBackStackEntry != null) {
                        myDishNavController.popBackStack()
                    }
                },
                onAddDishClick= {
                    myDishNavController.navigate("dish_edit_screen/ ")
                }
            )
        }
        composable(route = Screen.DishDetailScreen.route) {backStackEntry ->
            val dishId = backStackEntry.arguments?.getString("dishId") ?: "dish_001"
            DishDetailScreen(
                dishId = dishId,
                onEditDish = {dishId ->
                    myDishNavController.navigate("dish_edit_screen/$dishId")
                    },
                onBackClick= {
                    if (myDishNavController.previousBackStackEntry != null) {
                        myDishNavController.popBackStack()
                    }
                }
            )
        }
        composable(route = Screen.DishEditScreen.route) {backStackEntry ->
            val dishId = backStackEntry.arguments?.getString("dishId") ?: "dish_001"
            DishEditScreen(
                dishId = dishId,
                onCancel= {
                    if (myDishNavController.previousBackStackEntry != null) {
                        myDishNavController.popBackStack()
                    }
                }
            )
        }
    }
}
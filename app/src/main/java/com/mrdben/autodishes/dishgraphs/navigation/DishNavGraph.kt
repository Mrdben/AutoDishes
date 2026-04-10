package com.mrdben.autodishes.dishgraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.mrdben.autodishes.dishgraphs.screens.DishListScreen
import com.mrdben.autodishes.dishgraphs.screens.DishDetailScreen
import com.mrdben.autodishes.dishgraphs.screens.DishEditScreen

sealed class DishScreen(val route: String) {
    object DishList : DishScreen("dish_list_screen")
    object DishDetail : DishScreen("dish_detail_screen/{dishId}")
    object DishEdit : DishScreen("dish_edit_screen/{dishId}")
}

fun NavGraphBuilder.dishNavGraph(navController: NavHostController) {
    navigation(
        startDestination = DishScreen.DishList.route,
        route = "dish_graph"
    ) {
        composable(DishScreen.DishList.route) {
            DishListScreen(
                onDishClick = { dishId ->
                    navController.navigate("dish_detail_screen/$dishId")
                },
                onBackClick = { navController.popBackStack() },
                onAddDishClick = {
                    navController.navigate("dish_edit_screen/")
                }
            )
        }

        composable(DishScreen.DishDetail.route) { backStackEntry ->
            val dishId = backStackEntry.arguments?.getString("dishId") ?: "dish_001"
            DishDetailScreen(
                dishId = dishId,
                onEditDish = { dishId ->
                    navController.navigate("dish_edit_screen/$dishId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(DishScreen.DishEdit.route) { backStackEntry ->
            val dishId = backStackEntry.arguments?.getString("dishId") ?: ""
            DishEditScreen(
                dishId = dishId,
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
package com.mrdben.autodishes.dishgraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mrdben.autodishes.authgraphs.navigation.AuthScreen

import com.mrdben.autodishes.dishgraphs.screens.DishListScreen
import com.mrdben.autodishes.dishgraphs.screens.DishDetailScreen
import com.mrdben.autodishes.dishgraphs.screens.DishEditScreen
import com.mrdben.autodishes.familygraphs.navigation.FamilyScreen
import com.mrdben.autodishes.ordergraphs.navigation.OrderScreen

sealed class DishScreen(val route: String) {
    object DishRoot : DishScreen ("dish_graph")
    object DishList : DishScreen("dish_list_screen/{selectable}") {
        fun createRoute(selectable : Boolean = false): String{
            return "dish_list_screen/${if (selectable) "selectable" else ""}"
        }
    }
    object DishDetail : DishScreen("dish_detail_screen/{dishId}") {
        fun createRoute(dishId: String, readOnly: Boolean = false): String {
            return "dish_detail_screen/$dishId${if (readOnly) "&readonly" else ""}"
        }
    }
    object DishEdit : DishScreen("dish_edit_screen/{dishId}"){
        fun createRoute(dishId: String): String {
            return "dish_edit_screen/$dishId"
        }
    }
}

fun NavGraphBuilder.dishNavGraph(navController: NavHostController) {
    navigation(
        startDestination = DishScreen.DishList.route,
        route = DishScreen.DishRoot.route
    ) {
        composable(DishScreen.DishList.route) { backStackEntry ->
            val routePart = backStackEntry.arguments
                ?.getString("selectable")
                ?: ""
            val selectable = routePart == "selectable"
            DishListScreen(
                selectable = selectable,
                onDishClick = { dishId ->
                    navController.navigate(DishScreen.DishDetail.createRoute(dishId))
                },
                onBackClick = { navController.popBackStack() },
                onAddDishClick = {
                    navController.navigate(DishScreen.DishEdit.createRoute(" "))
                },
                onViewElderList = { navController.navigate(FamilyScreen.MyFamily.route) },
                onViewOrderList = { navController.navigate(OrderScreen.OrderList.route) },
                onMakeMealClick = { navController.navigate(OrderScreen.MakeMeal.route) },
                onUserProfile = { navController.navigate(AuthScreen.Profile.route) }
            )
        }

        composable(DishScreen.DishDetail.route) { backStackEntry ->
            val routePart = backStackEntry.arguments
                ?.getString("dishId")
                ?: "dish_001"
            val parts = routePart.split("&")
            val dishId = parts[0]
            val readOnly = parts.getOrNull(1) == "readonly"
            DishDetailScreen(
                dishId = dishId,
                readOnly = readOnly,
                onEditDish = { dishId ->
                    navController.navigate(DishScreen.DishEdit.createRoute(dishId))
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
package com.mrdben.autodishes.familygraphs.navigation

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mrdben.autodishes.dishgraphs.navigation.DishScreen

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen
import com.mrdben.autodishes.familygraphs.screens.ElderDetailScreen
import com.mrdben.autodishes.familygraphs.screens.ElderEditScreen
import com.mrdben.autodishes.ordergraphs.navigation.OrderScreen

sealed class FamilyScreen(val route: String) {
    object MyFamily : FamilyScreen("my_family_screen")
    object ElderDetail : FamilyScreen("elder_details/{elderId}") {
        fun createRoute(elderId: String, readOnly: Boolean): String {
            return "elder_details/$elderId${if (readOnly) "&readonly" else ""}"
        }
    }
    object EditElder : FamilyScreen("edit_elder/{elderId}")
}

// 子导航图（对外暴露）
fun NavGraphBuilder.familyNavGraph(navController: NavHostController) {
    navigation(
        startDestination = FamilyScreen.MyFamily.route,
        route = "family_graph"
    ) {
        composable(FamilyScreen.MyFamily.route) {
            MyFamilyScreen(
                onAddElderClick = {
                    navController.navigate("edit_elder/")
                },
                onBackClick = { navController.popBackStack() },
                onElderClick = { elderId ->
                    navController.navigate("elder_details/$elderId")
                },
                onViewDishList = {navController.navigate(DishScreen.DishList.route)},
                onViewOrderList = {navController.navigate(OrderScreen.OrderList.route)}
            )
        }

        composable(FamilyScreen.ElderDetail.route) { backStackEntry ->
            val routePart = backStackEntry.arguments
                ?.getString("elderId")
                ?: "debug_elder_001"
            val parts = routePart.split("&")
            val elderId = parts[0]
            val readOnly = parts.getOrNull(1) == "readonly"
            ElderDetailScreen(
                elderId = elderId,
                readOnly = readOnly,
                onBackClick = { navController.popBackStack() },
                onEditElder = { elderId ->
                    navController.navigate("edit_elder/$elderId")
                }
            )
        }

        composable(FamilyScreen.EditElder.route) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") ?: ""
            ElderEditScreen(
                elderId = elderId,
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
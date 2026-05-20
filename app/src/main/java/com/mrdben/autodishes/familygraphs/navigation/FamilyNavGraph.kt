package com.mrdben.autodishes.familygraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mrdben.autodishes.authgraphs.navigation.AuthScreen
import com.mrdben.autodishes.dishgraphs.navigation.DishScreen

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen
import com.mrdben.autodishes.familygraphs.screens.ElderDetailScreen
import com.mrdben.autodishes.familygraphs.screens.ElderEditScreen
import com.mrdben.autodishes.ordergraphs.navigation.OrderScreen

sealed class FamilyScreen(val route: String) {
    object FamilyRoot : FamilyScreen ("family_graph")
    object MyFamily : FamilyScreen("my_family_screen/{selectable}") {
        fun createRoute(selectable : Boolean = false): String{
            return "my_family_screen/${if (selectable) "selectable" else ""}"
        }
    }
    object ElderDetail : FamilyScreen("elder_details/{elderId}") {
        fun createRoute(elderId: String, readOnly: Boolean = false): String {
            return "elder_details/$elderId${if (readOnly) "&readonly" else ""}"
        }
    }
    object EditElder : FamilyScreen("edit_elder/{elderId}") {
        fun createRoute(elderId: String): String {
            return "edit_elder/$elderId"
        }
    }
}

// 子导航图（对外暴露）
fun NavGraphBuilder.familyNavGraph(navController: NavHostController) {
    navigation(
        startDestination = FamilyScreen.MyFamily.route,
        route = FamilyScreen.FamilyRoot.route
    ) {
        composable(FamilyScreen.MyFamily.route) { backStackEntry ->
            val routePart = backStackEntry.arguments
                ?.getString("selectable")
                ?: ""
            val selectable = routePart == "selectable"
            MyFamilyScreen(
                selectable = selectable,
                onAddElderClick = {
                    navController.navigate(FamilyScreen.EditElder.createRoute(" "))
                },
                onBackClick = { navController.popBackStack() },
                onElderClick = { elderId ->
                    navController.navigate(FamilyScreen.ElderDetail.createRoute(elderId = elderId, readOnly = selectable))
                },
                onViewDishList = {navController.navigate(DishScreen.DishList.route)},
                onViewOrderList = {navController.navigate(OrderScreen.OrderList.route)},
                onMakeMealClick = {navController.navigate(OrderScreen.MakeMeal.route) {
                    if (!selectable) popUpTo(0) {inclusive = true}
                } },
                onUserProfile = { navController.navigate(AuthScreen.Profile.route)}
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
                    navController.navigate(FamilyScreen.EditElder.createRoute(elderId))
                },
                onToElderList = {navController.navigate(FamilyScreen.MyFamily.route) {
                    popUpTo(FamilyScreen.MyFamily.route) {inclusive = true}
                } }
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
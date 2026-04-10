package com.mrdben.autodishes.familygraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.mrdben.autodishes.familygraphs.screens.MyFamilyScreen
import com.mrdben.autodishes.familygraphs.screens.ElderDetailScreen
import com.mrdben.autodishes.familygraphs.screens.ElderEditScreen

sealed class FamilyScreen(val route: String) {
    object MyFamily : FamilyScreen("my_family_screen")
    object ElderDetail : FamilyScreen("elder_details/{elderId}")
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
                }
            )
        }

        composable(FamilyScreen.ElderDetail.route) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") ?: "debug_elder_001"
            ElderDetailScreen(
                elderId = elderId,
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
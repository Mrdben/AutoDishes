package com.mrdben.autodishes.ordergraphs.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.ordergraphs.screens.OrderListScreen
import com.mrdben.autodishes.ordergraphs.screens.OrderDetailScreen


// 定义路由常量
sealed class Screen(val route: String) {
    object OrderListScreen : Screen("order_list_screen")
    object OrderDetailScreen : Screen("order_detail_screen/{orderId}")
}

@Composable
fun SetupOrderNavGraph() {
    val myOrderNavController = rememberNavController()

    NavHost(
        navController = myOrderNavController,
        startDestination = Screen.OrderListScreen.route
    ) {
        composable(route = Screen.OrderListScreen.route) {
            OrderListScreen(
                onOrderClick = {orderId ->
                    myOrderNavController.navigate("order_detail_screen/$orderId")
                               },
                onBackClick= {
                    if (myOrderNavController.previousBackStackEntry != null) {
                        myOrderNavController.popBackStack()
                    }
                }
            )
        }
        composable(route = Screen.OrderDetailScreen.route) {backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "order_001"
            OrderDetailScreen(
                orderId = orderId,
                onBackClick= {
                    if (myOrderNavController.previousBackStackEntry != null) {
                        myOrderNavController.popBackStack()
                    }
                }
            )
        }
    }
}
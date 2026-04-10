package com.mrdben.autodishes.ordergraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.mrdben.autodishes.ordergraphs.screens.OrderListScreen
import com.mrdben.autodishes.ordergraphs.screens.OrderDetailScreen


sealed class OrderScreen(val route: String) {
    object OrderList : OrderScreen("order_list_screen")
    object OrderDetail : OrderScreen("order_detail_screen/{orderId}")
}

fun NavGraphBuilder.orderNavGraph(navController: NavHostController) {
    navigation(
        startDestination = OrderScreen.OrderList.route,
        route = "order_graph"
    ) {
        composable(OrderScreen.OrderList.route) {
            OrderListScreen(
                onOrderClick = { orderId ->
                    navController.navigate("order_detail_screen/$orderId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(OrderScreen.OrderDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "order_001"
            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },

                // ✅ 跨组件跳转到【家人详情】
                onElderClick = { elderId ->
                    navController.navigate("elder_details/$elderId")
                },

                // ✅ 跨组件跳转到【菜品详情】
                onDishClick = { dishId ->
                    navController.navigate("dish_detail_screen/$dishId")
                }
            )
        }
    }
}
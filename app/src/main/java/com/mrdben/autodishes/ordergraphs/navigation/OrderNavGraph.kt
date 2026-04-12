package com.mrdben.autodishes.ordergraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mrdben.autodishes.dishgraphs.navigation.DishScreen
import com.mrdben.autodishes.familygraphs.navigation.FamilyScreen
import com.mrdben.autodishes.ordergraphs.screens.MakeMealScreen

import com.mrdben.autodishes.ordergraphs.screens.OrderListScreen
import com.mrdben.autodishes.ordergraphs.screens.OrderDetailScreen


sealed class OrderScreen(val route: String) {
    object OrderList : OrderScreen("order_list_screen")
    object OrderDetail : OrderScreen("order_detail_screen/{orderId}")
    object MakeMeal : OrderScreen("make_meal_screen")
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
                onBackClick = { navController.popBackStack() },
                onMakeMealClick = {navController.navigate(OrderScreen.MakeMeal.route)},
                onViewElderList = {navController.navigate(FamilyScreen.MyFamily.route)},
                onViewDishList = {navController.navigate(DishScreen.DishList.route)}
            )
        }

        composable(OrderScreen.OrderDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: "order_001"
            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },

                // ✅ 跨组件跳转到【家人详情】
                onElderClick = { elderId ->
                    navController.navigate(
                        FamilyScreen.ElderDetail.createRoute(elderId, readOnly = true)
                    )
                },

                // ✅ 跨组件跳转到【菜品详情】
                onDishClick = { dishId ->
                    navController.navigate(
                        DishScreen.DishDetail.createRoute(dishId, readOnly = true)
                    )
                },
                onViewOrderList = {
                    navController.navigate(
                        OrderScreen.OrderList.route
                    )
                }
            )
        }

        composable(OrderScreen.MakeMeal.route) {
            MakeMealScreen(
                onBackClick = { navController.popBackStack() },
                onElderClick = { elderId ->
                    navController.navigate(FamilyScreen.ElderDetail.createRoute(elderId, readOnly = true))
                },
                onDishClick = { dishId ->
                    navController.navigate(DishScreen.DishDetail.createRoute(dishId, readOnly = true))
                },
                onConfirmOrder = { orderId ->
                    navController.navigate("order_detail_screen/$orderId")
                },
                onSelectElder = {navController.navigate(FamilyScreen.MyFamily.createRoute(selectable = true))},
                onSelectDish = {navController.navigate(DishScreen.DishList.createRoute(selectable = true))}
            )
        }

    }
}
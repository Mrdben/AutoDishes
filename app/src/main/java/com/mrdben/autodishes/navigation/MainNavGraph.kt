package com.mrdben.autodishes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mrdben.autodishes.api.AuthEventBus
import com.mrdben.autodishes.familygraphs.navigation.familyNavGraph
import com.mrdben.autodishes.dishgraphs.navigation.dishNavGraph
import com.mrdben.autodishes.ordergraphs.navigation.orderNavGraph
import com.mrdben.autodishes.authgraphs.navigation.authNavGraph
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        AuthEventBus.forceLogoutFlow.collect {
            // 收到信号！执行统一的跳转逻辑
            navController.navigate("login_screen/warning") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = "auth_graph" // 默认打开订单模块
    ) {
        authNavGraph(navController)
        familyNavGraph(navController)
        dishNavGraph(navController)
        orderNavGraph(navController)
    }
}

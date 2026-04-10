package com.mrdben.autodishes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mrdben.autodishes.familygraphs.navigation.familyNavGraph
import com.mrdben.autodishes.dishgraphs.navigation.dishNavGraph
import com.mrdben.autodishes.ordergraphs.navigation.orderNavGraph

@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "order_graph" // 默认打开订单模块
    ) {
        // 加载 3 个独立的子导航图（完全解耦）
        familyNavGraph(navController)
        dishNavGraph(navController)
        orderNavGraph(navController)
    }
}

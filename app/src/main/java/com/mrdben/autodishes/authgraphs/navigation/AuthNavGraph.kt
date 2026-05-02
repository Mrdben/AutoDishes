package com.mrdben.autodishes.authgraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.mrdben.autodishes.authgraphs.screens.SplashScreen
import com.mrdben.autodishes.authgraphs.screens.LoginScreen
import com.mrdben.autodishes.authgraphs.screens.RegisterScreen
import com.mrdben.autodishes.authgraphs.screens.UpdateUserScreen
import com.mrdben.autodishes.authgraphs.screens.UserProfileScreen
import com.mrdben.autodishes.familygraphs.navigation.FamilyScreen

// 定义路由常量
sealed class AuthScreen(val route: String) {
    object AuthRoot : AuthScreen("auth_graph")
    object Splash : AuthScreen("splash_screen")
    object Login : AuthScreen("login_screen")
    object Profile : AuthScreen("user_profile")
    object Register : AuthScreen("registry_screen")
    object Update : AuthScreen("update_profile/{style}") {
        fun createRoute(style: String): String {
            return "update_profile/$style"
        }
    }
}

private fun navigateToLogin(navController: NavHostController) {
    navController.navigate(AuthScreen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}
private fun navigateToFamily(navController: NavHostController) {
    navController.navigate(FamilyScreen.FamilyRoot.route) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthScreen.Splash.route,
        route = AuthScreen.AuthRoot.route
    )  {
        composable(route = AuthScreen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = { navigateToLogin(navController) },
                onNavigateToContent = { navigateToFamily(navController) }
            )
        }
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onNavigateToRegistry = { navController.navigate(AuthScreen.Register.route) },
                onNavigateToForgetPassword ={ navController.navigate(AuthScreen.Update.createRoute("forgot")) },
                onSignOn = { navigateToFamily(navController) }
            )
        }
        composable(route = AuthScreen.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onSuccess = { navigateToLogin(navController) }
            )
        }
        composable(route = AuthScreen.Update.route) { backStackEntry ->
            val style = backStackEntry.arguments?.getString("style") ?: "password"
            UpdateUserScreen(
                style = style,
                onBackClick = { navController.popBackStack() },
                onLogoff ={ navigateToLogin(navController) }
            )
        }
        composable(route = AuthScreen.Profile.route) {
            UserProfileScreen(
                onBackClick = { navController.popBackStack() },
                onChangePassword = { navController.navigate(AuthScreen.Update.createRoute("password")) },
                onUpdatePhoneNumber= {  navController.navigate(AuthScreen.Update.createRoute("phone")) },
                onLogoff = { navigateToLogin(navController) },
                onDeleteUser = { navController.navigate(AuthScreen.Update.createRoute("delete")) }
            )
        }
    }
}
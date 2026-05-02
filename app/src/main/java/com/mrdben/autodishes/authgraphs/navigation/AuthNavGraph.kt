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
    object Update : AuthScreen("update_profile")
}

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthScreen.Splash.route,
        route = AuthScreen.AuthRoot.route
    )  {
        composable(route = AuthScreen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToContent = {
                    navController.navigate(FamilyScreen.FamilyRoot.route) {
                        popUpTo(AuthScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onNavigateToRegistry = {
                    navController.navigate(AuthScreen.Register.route)
                    },
                onNavigateToForgetPassword ={
                    navController.navigate(AuthScreen.Update.route)
                },
                onSignOn = {
                    navController.navigate(FamilyScreen.FamilyRoot.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(route = AuthScreen.Register.route) {
            RegisterScreen(
                onBackClick = {navController.popBackStack()},
                onSuccess = {navController.navigate(AuthScreen.Login.route)}
            )
        }
        composable(route = AuthScreen.Update.route) {
            UpdateUserScreen(
                style = "password",
                onBackClick = { navController.popBackStack() },
                onLogoff = { navController.navigate(AuthScreen.Login.route) },
            )
        }
        composable(route = AuthScreen.Profile.route) {
            UserProfileScreen(
                onBackClick = { navController.popBackStack() },
                onChangePassword = { navController.navigate(AuthScreen.Update.route) },
                onUpdatePhoneNumber= {},
                onLogoff = { navController.navigate(AuthScreen.Login.route) }
            )
        }
    }
}
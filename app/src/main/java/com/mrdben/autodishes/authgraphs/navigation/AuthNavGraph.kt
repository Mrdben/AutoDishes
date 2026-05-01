package com.mrdben.autodishes.authgraphs.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.mrdben.autodishes.authgraphs.screens.SplashScreen
import com.mrdben.autodishes.authgraphs.screens.LoginScreen
import com.mrdben.autodishes.authgraphs.screens.RegisterScreen
import com.mrdben.autodishes.authgraphs.screens.ForgotPasswordScreen
import com.mrdben.autodishes.authgraphs.screens.ResetPasswordScreen
import com.mrdben.autodishes.familygraphs.navigation.FamilyScreen

// 定义路由常量
sealed class AuthScreen(val route: String) {
    object AuthRoot : AuthScreen("auth_graph")
    object Splash : AuthScreen("splash_screen")
    object Login : AuthScreen("login_screen")
    object Register : AuthScreen("registry_screen")
    object Forgot : AuthScreen("forgot_password")
    object Reset :  AuthScreen("reset_password")
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
                    navController.navigate(AuthScreen.Forgot.route)
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
                onCancel = {navController.popBackStack()},
                onSuccess = {navController.navigate(AuthScreen.Login.route)}
            )
        }
        composable(route = AuthScreen.Forgot.route) {
            ForgotPasswordScreen(
                onNavigateToLogin ={
                    navController.navigate(AuthScreen.Login.route)
                }
            )
        }
        composable(route = AuthScreen.Reset.route) {
            ResetPasswordScreen(
            )
        }
    }
}
package com.mrdben.autodishes.authgraphs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mrdben.autodishes.authgraphs.screens.SplashScreen
import com.mrdben.autodishes.authgraphs.screens.LoginScreen
import com.mrdben.autodishes.authgraphs.screens.RegisterScreen
import com.mrdben.autodishes.authgraphs.screens.ForgotPasswordScreen
import com.mrdben.autodishes.authgraphs.screens.ResetPasswordScreen

// 定义路由常量
sealed class Screen(val route: String) {
    object Splash : Screen("splash_sceen")
    object Login : Screen("login_screen")
    object Register : Screen("registry_screen")
    object Forgot : Screen("forgot_password")
    object Reset :  Screen("reset_password")
}

@Composable
fun SetupAuthNavGraph() {
    val authNavController = rememberNavController()

    NavHost(
        navController = authNavController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    authNavController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegistry = {
                    authNavController.navigate(Screen.Register.route) {}
                    },
                onNavigateToForgetPassword ={
                    authNavController.navigate(Screen.Forgot.route) {}
                }
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen()
        }
        composable(route = Screen.Forgot.route) {
            ForgotPasswordScreen(
                onNavigateToLogin ={
                    authNavController.navigate(Screen.Login.route)
                }
            )
        }
        composable(route = Screen.Reset.route) {
            ResetPasswordScreen(
            )
        }
    }
}
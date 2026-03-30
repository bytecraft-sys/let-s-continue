package com.example.letscontinue

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    val viewModel: AuthViewModel  = viewModel()

    val startDestination =
        if (viewModel.isUserLoggedIn()) "chat"
        else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginPage(
                onSignupClick = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate("chat") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onLoginClick = { navController.navigate("login") },
                onSignupSuccess = {
                    navController.navigate("chat") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        composable("chat") {
            ChatListSr(
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("chat") { inclusive = true }
                    }
                },
                onOpenChat = { userId ->
                    navController.navigate("chat_screen/$userId")
                }
            )
        }

        composable("chat_screen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            ChatScreen(
                otherUserId = userId,
                currentUserId = viewModel.getCurrentUserId(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
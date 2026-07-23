package com.example.letscontinue.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.letscontinue.QRScreen
import com.example.letscontinue.screen.LoginPage
import com.example.letscontinue.screen.ChatListSr
import com.example.letscontinue.screen.ChatScreen
import com.example.letscontinue.screen.SignupScreen
import com.example.letscontinue.viewmodel.AuthViewModel
import com.example.letscontinue.viewmodel.ChatViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onToggleTheme: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()

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
                },
                onQrClick = { navController.navigate("qr_screen") },
                onToggleTheme = onToggleTheme
            )
        }

        composable("chat_screen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatViewModel: ChatViewModel = viewModel()


            ChatScreen(
                otherUserId = userId,
                currentUserId = viewModel.getCurrentUserId(),
                onBack = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }

        composable("qr_screen") {
            QRScreen(
                onBack = { navController.popBackStack() },
                onUserFound = { userId ->
                    navController.navigate("chat_screen/$userId")
                }
            )
        }
    }

}
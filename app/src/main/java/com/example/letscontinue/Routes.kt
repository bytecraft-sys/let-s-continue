package com.example.letscontinue

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ChatList : Screen("chatList")
}
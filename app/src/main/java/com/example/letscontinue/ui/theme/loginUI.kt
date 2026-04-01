package com.example.letscontinue

import com.google.firebase.Timestamp


data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)
data class SignupUiState(
    val name: String="",
    val email: String="",
    val password: String="",
    val confirmPassword: String="",

    val nameError: String?=null,
    val emailError: String?=null,
    val passwordError: String?=null,
    val confirmPasswordError: String?=null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null

    )

data class ChatItem(
    val name: String,
    val message: String,
    val time: String,
    val unreadCount:Int = 0
)

data class User(
    val uid: String="",
    val name: String="",
   val email: String=""
)
data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp= Timestamp.now()
)
package com.example.letscontinue.screen

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letscontinue.ui.theme.AuthBackground
import com.example.letscontinue.ui.theme.chatitems.ChatTopBar
import com.example.letscontinue.ui.theme.chatitems.MessageBubble
import com.example.letscontinue.ui.theme.chatitems.MessageInputBar
import com.example.letscontinue.viewmodel.ChatViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ServiceCast")
@Composable
fun ChatScreen(
    otherUserId: String,
    currentUserId: String,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val otherUserName by chatViewModel.otherUserName.collectAsState()
    val otherUserOnline by chatViewModel.otherUserOnline.collectAsState()
    val isOtherTyping by chatViewModel.isOtherTyping.collectAsState()
    val context = LocalContext.current

    val isConnected = remember {
        val cm = context.getSystemService(
            android.content.Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    LaunchedEffect(otherUserId) {
        chatViewModel.loadChat(otherUserId)
        chatViewModel.setOnline(true)
        chatViewModel.listenForTyping(otherUserId)
        chatViewModel.markMessagesAsRead(otherUserId)
        chatViewModel.markAsDelivered(otherUserId)
    }

    DisposableEffect(Unit) {
        onDispose { chatViewModel.setOnline(false) }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ChatTopBar(
            name = otherUserName,
            isOnline = otherUserOnline,
            onBack = onBack
        )

        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No internet connection",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Use weight(1f) here to make the background fill the remaining space
        AuthBackground(modifier = Modifier.weight(1f)) {
            // Added Column to provide ColumnScope for .weight(1f) and keep input bar at bottom
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f) // Now valid inside ColumnScope
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isMe = message.senderId == currentUserId,
                            onDeleteForMe = {
                                chatViewModel.deleteForMe(otherUserId, message.id)
                            },
                            onDeleteForEveryone = {
                                chatViewModel.deleteForEveryone(otherUserId, message.id)
                            }
                        )
                    }
                }

                MessageInputBar(
                    onSendMessage = { text ->
                        chatViewModel.sendMessage(otherUserId, text)
                    },
                    onTyping = { isTyping ->
                        chatViewModel.setTyping(otherUserId, isTyping)
                    }
                )
            }
        }
    }
}

fun formatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
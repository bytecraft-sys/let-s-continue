package com.example.letscontinue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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

    LaunchedEffect(otherUserId) {
        chatViewModel.loadChat(otherUserId)
        chatViewModel.setOnline(true)
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
            .background(Color(0xFFF0EEF8))
    ) {
        ChatTopBar(
            name = otherUserName,
            isOnline = otherUserOnline,
            onBack = onBack
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isMe = message.senderId == currentUserId
                )
            }
        }

        MessageInputBar(
            onSendMessage = { text ->
                chatViewModel.sendMessage(otherUserId, text)
            }
        )
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(
                        brush = if (isMe)
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                            )
                        else
                            Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFF8F8F8))
                            ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else Color(0xFF1A1A2E),
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(message.timestamp as? Timestamp),
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F1F1),
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale.value)
                    .background(
                        if (text.isNotBlank())
                            Brush.horizontalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFFBBBBBB), Color(0xFFCCCCCC))),
                        shape = CircleShape
                    )
                    .clickable {
                        if (text.isNotBlank()) {
                            scope.launch {
                                scale.animateTo(0.85f)
                                scale.animateTo(1f)
                            }
                            onSendMessage(text.trim())
                            text = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatTopBar(
    name: String,
    isOnline: Boolean,
    onBack: () -> Unit
) {
    Surface(shadowElevation = 4.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifEmpty { "Loading..." },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A2E)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isOnline) Color(0xFF4CAF50) else Color.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        fontSize = 12.sp,
                        color = if (isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }

            IconButton(onClick = { /* TODO: Video call */ }) {
                Icon(Icons.Default.VideoCall, contentDescription = "Video Call", tint = Color(0xFF4A00E0))
            }
            IconButton(onClick = { /* TODO: Voice call */ }) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4A00E0))
            }
        }
    }
}

fun formatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
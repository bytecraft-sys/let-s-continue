package com.example.letscontinue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<Message>,
    currentUserId: String,
    onSendMessage:(String)-> Unit
){
    val listState= rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()){
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    Column (
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ){
        ChatTopBar()
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages){message->
                MessageBubble(
                    message=message,
                    isMe=message.senderId == currentUserId
                )
            }
        }
        MessageInputBar(onSendMessage)
    }
}
@Composable
fun MessageBubble(message: Message,
                  isMe: Boolean){
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
    ) {
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe)Arrangement.End else Arrangement.Start){
            Box(
                modifier = Modifier.
                widthIn(max = 260.dp).
                shadow(elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                    .background(
                        brush = if (isMe)
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF4A00E0),
                                    Color(0xFF8E2DE2)
                                )
                            )
                        else
                            Brush.verticalGradient(
                                listOf(
                                    Color.White,
                                    Color(0xFFF1F1F1)
                                )
                            ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = formatTime(message.timestamp as Timestamp?),
                        fontSize = 11.sp,
                        color = if (isMe)Color.White.copy(alpha = 0.7f)
                        else Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    onSendMessage: (String) -> Unit
){
    var text by remember { mutableStateOf("") }
    val  scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(   8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        TextField(
            value = text,
            onValueChange = {text = it},
            placeholder = {Text("Type a message")},
            modifier = Modifier.weight(1f),
            shape=RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F1F1),
                unfocusedContainerColor = Color(0xFFF1F1F1),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale.value)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF4A00E0),
                            Color(0xFF8E2DE2)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable{
                    if (text.isNotBlank()){
                        scope.launch {
                            scale.animateTo(0.85f)
                            scale.animateTo(1f)
                        }
                        onSendMessage(text.trim())
                        text = ""
                    }
                },
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun ChatTopBar(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
         verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            Icons.Default.ArrowBackIosNew, null)
        Spacer(modifier = Modifier.width(12.dp))

        Column (modifier = Modifier.weight(1f)){
            Text(
                text = "",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "online",
                fontSize = 12.sp,
                color = Color.Green
            )
        }

        Icon(Icons.Default.VideoCall,null)
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.Call,null)
    }
}
fun formatTime(timestamp: Timestamp?): String{
    if (timestamp==null)return ""
    val sdf= SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
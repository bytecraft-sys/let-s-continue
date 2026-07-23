package com.example.letscontinue.ui.theme.chatitems

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letscontinue.ui.theme.Message
import com.example.letscontinue.screen.formatTime
import com.google.firebase.Timestamp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onDeleteForMe:()-> Unit,
    onDeleteForEveryone: () -> Unit

) {
    var showMenu by remember { mutableStateOf(false) }

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
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        else
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true },
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = if (message.deletedForEveryone)
                            Color.Gray
                        else if (isMe) Color.White
                        else MaterialTheme.colorScheme.onSurface,
                        fontSize = if (message.deletedForEveryone) 13.sp else 15.sp,
                        fontStyle = if (message.deletedForEveryone)
                            androidx.compose.ui.text.font.FontStyle.Italic
                        else
                            androidx.compose.ui.text.font.FontStyle.Normal,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = formatTime(message.timestamp as? Timestamp),
                            fontSize = 10.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                        if (isMe && !message.deletedForEveryone) {
                            MessageStatusIcon(status = message.status)
                        }
                    }
                }
            }
// Dropdown menu on long press
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete for me") },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, null, tint = Color.Gray)
                    },
                    onClick = {
                        showMenu = false
                        onDeleteForMe()
                    }
                )
                // Only show "delete for everyone" for MY messages
                if (isMe && !message.deletedForEveryone) {
                    DropdownMenuItem(
                        text = { Text("Delete for everyone", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            showMenu = false
                            onDeleteForEveryone()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(status: String) {
    val icon = when (status) {
        "read" -> "✓✓"
        "delivered" -> "✓✓"
        else -> "✓"
    }
    val color = when (status) {
        "read" -> Color(0xFF4FC3F7)  // blue
        else -> Color.White.copy(alpha = 0.7f)  // white/grey
    }
    Text(
        text = icon,
        fontSize = 10.sp,
        color = color,
        fontWeight = FontWeight.Bold
    )
}
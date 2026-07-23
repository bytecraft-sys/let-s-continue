package com.example.letscontinue.ui.theme.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letscontinue.ui.theme.User
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun ChatListItem(user: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatar(
                    photoUrl = user.photoUrl,
                    name = user.name,
                    size = 52,
                    fontSize = 20
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name + last message
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = user.lastMessage.ifEmpty { "Tap to start chatting" },
                    fontSize = 13.sp,
                    color = if (user.lastMessage.isEmpty()) Color(0xFFBBBBBB) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = if (user.lastMessage.isEmpty())
                        androidx.compose.ui.text.font.FontStyle.Italic
                    else
                        androidx.compose.ui.text.font.FontStyle.Normal
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time on the right
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatChatTime(user.lastMessageTime),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// Smart time formatter
fun formatChatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val now = java.util.Date()
    val msgDate = timestamp.toDate()
    val diffMs = now.time - msgDate.time
    val diffHours = diffMs / (1000 * 60 * 60)
    val diffDays = diffMs / (1000 * 60 * 60 * 24)

    return when {
        diffHours < 24 -> SimpleDateFormat("hh:mm a", Locale.getDefault()).format(msgDate)
        diffDays < 7  -> SimpleDateFormat("EEE", Locale.getDefault()).format(msgDate) // Mon, Tue
        else          -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(msgDate)
    }
}
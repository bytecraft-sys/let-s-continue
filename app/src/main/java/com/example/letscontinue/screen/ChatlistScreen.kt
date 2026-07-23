package com.example.letscontinue.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letscontinue.NewChatDialog
import com.example.letscontinue.saveTokenToFirestore
import com.example.letscontinue.ui.theme.User
import com.example.letscontinue.ui.theme.chatlist.BottomNavigationBar
import com.example.letscontinue.ui.theme.chatlist.ChatListItem
import com.example.letscontinue.ui.theme.chatlist.FilterChips
import com.example.letscontinue.ui.theme.chatlist.TopSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChatListSr(
    onLogout: () -> Unit,
    onOpenChat: (String) -> Unit,
    onQrClick: () -> Unit,
    onToggleTheme: () -> Unit

) {
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var myReferralCode by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    val filteredUsers = remember(userList, searchQuery) {
        if (searchQuery.isBlank()) userList
        else userList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }


    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUid = auth.currentUser?.uid

        if (currentUid == null) return@DisposableEffect onDispose {}

        // Listener 1 — my profile (for referral code)
        val profileListener = firestore.collection("users")
            .document(currentUid)
            .addSnapshotListener { myDoc, _ ->
                myReferralCode = myDoc?.getString("referralCode") ?: ""
            }

                //listener 2
                val connectionsListener = firestore.collection("users")
                    .document(currentUid)
                    .collection("connections")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) return@addSnapshotListener
                        snapshots?.documents?.forEach { connDoc->
                            val uid = connDoc.getString("uid") ?: return@forEach
                            firestore.collection("users").document(uid)
                                .addSnapshotListener { userDoc, _ ->
                                    if (userDoc == null) return@addSnapshotListener
                                    val user = User(
                                        uid = userDoc.id,
                                        email = userDoc.getString("email") ?: "",
                                        name = userDoc.getString("name") ?: "User",
                                        photoUrl = userDoc.getString("photoUrl")
                                    )
                                    val chatId = getChatId(currentUid, uid)
                                    firestore.collection("chats").document(chatId)
                                        .addSnapshotListener { chatDoc, _ ->
                                            val lastMessage = chatDoc?.getString("lastMessage") ?: ""
                                            val lastTime = chatDoc?.getTimestamp("lastTimestamp")
                                            userList =(userList+user.copy(
                                                lastMessage=lastMessage,
                                                lastMessageTime= lastTime))
                                                .distinctBy {it.uid }
                                                .sortedByDescending { it.lastMessageTime?.seconds?:0 }
                                        }
                                }
                        }
                    }

                // Listener 3 - referral-based connections (backward compat)
                val referralListener = firestore.collection("users")
                    .whereEqualTo("referredBy", currentUid)
                    .addSnapshotListener { documents, _ ->
                        documents?.forEach { doc ->
                            val uid = doc.id
                            val user = User(
                                uid = uid,
                                name = doc.getString("name") ?: "User",
                                email = doc.getString("email") ?: "",
                                photoUrl = doc.getString("photoUrl")
                            )
                            val chatId = getChatId(currentUid, uid)
                            firestore.collection("chats").document(chatId)
                                .addSnapshotListener { chatDoc, _ ->
                                    val lastMessage = chatDoc?.getString("lastMessage") ?: ""
                                    val lastTime = chatDoc?.getTimestamp("lastTimestamp")
                                    userList = (userList + user.copy(
                                        lastMessage = lastMessage,
                                        lastMessageTime = lastTime
                                    )).distinctBy { it.uid }
                                        .sortedByDescending { it.lastMessageTime?.seconds ?: 0 }
                                }
                        }
                    }

        // Save FCM token when user opens app
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveTokenToFirestore(token)
            }


        onDispose {
            profileListener.remove()
            connectionsListener.remove()
            referralListener.remove()
        }

    }
    // New chat dialog
    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onChatStarted = { userId ->
                showNewChatDialog = false
                onOpenChat(userId)
            }
        )
    }



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true},
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White)
            }
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Top bar with gradient header

                TopSection(
                    onLogout = onLogout,
                    referralCode = myReferralCode,
                    onQrClick = onQrClick,
                    onToggleTheme = onToggleTheme
                )


            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search conversations", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor =MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                FilterChips()
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFDDD8FF)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results found" else "No chats yet",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Share your referral code to connect",
                                color = Color(0xFFBBBBBB),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredUsers) { user ->
                        AnimatedVisibility(visible = true, enter = fadeIn()) {
                            ChatListItem(user = user, onClick = { onOpenChat(user.uid) })
                        }
                    }
                }
            }
        }
    }
}

fun saveToFirestore(field: String, value: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance().collection("users").document(uid).update(field, value)
}

fun getChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
}

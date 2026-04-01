package com.example.letscontinue.ui.theme.chatlist

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
import com.example.letscontinue.BottomNavigationBar
import com.example.letscontinue.ChatListItem
import com.example.letscontinue.FilterChips
import com.example.letscontinue.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChatListSr(
    onLogout: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var myReferralCode by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

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

                // Also load the person who referred ME
                val referredByUid = myDoc?.getString("referredBy")
                if (referredByUid != null) {
                    firestore.collection("users")
                        .document(referredByUid)
                        .get()
                        .addOnSuccessListener { doc ->
                            val referrer = User(
                                uid = doc.id,
                                email = doc.getString("email") ?: "",
                                name = doc.getString("name") ?: "User",
                            )

                            userList = (userList+referrer).distinctBy { it.uid }
                        }
                }
            }
        val usersListener = firestore.collection("users")
            .whereEqualTo("referredBy", currentUid)
            .addSnapshotListener { documents, error ->
                if (error != null) return@addSnapshotListener
                val referred = documents?.map { doc ->
                    User(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        name = doc.getString("name") ?: "User"
                    )
                } ?: emptyList()

                // Merge both lists without duplicates
                userList = (userList + referred).distinctBy { it.uid }
            }

        onDispose {
            profileListener.remove()
            usersListener.remove()
        }

    }

    val filteredUsers = remember(userList, searchQuery) {
        if (searchQuery.isBlank()) userList
        else userList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Show user search dialog to start new chat */ },
                containerColor = Color(0xFF4A00E0)
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
                .background(Color(0xFFF8F7FF))
                .padding(padding)
        ) {
            // Top bar with gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                TopSection(
                    onLogout = onLogout,
                    referralCode = myReferralCode
                )
            }

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
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF4A00E0))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A00E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
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
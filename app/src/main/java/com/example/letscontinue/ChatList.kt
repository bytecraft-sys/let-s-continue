package com.example.letscontinue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ChatListSr(
    onLogout: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var myReferralCode by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUid = auth.currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("users").document(currentUid)
            .addSnapshotListener { myDoc, _ ->
                myReferralCode = myDoc?.getString("referralCode") ?: ""
            }

        firestore.collection("users")
            .whereEqualTo("referredBy", currentUid)
            .addSnapshotListener { documents, error ->
                if (error != null) return@addSnapshotListener
                userList = documents?.map { doc ->
                    User(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        name = doc.getString("name") ?: "User"
                    )
                } ?: emptyList()
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

@Composable
fun ChatListItem(user: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCCCCCC)
            )
        }
    }
}

@Composable
fun TopSection(
    onLogout: () -> Unit,
    referralCode: String
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingBio by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                bio = document.getString("bio") ?: ""
            }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${name.ifEmpty { "You" }}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Column(modifier = Modifier.padding(16.dp).width(270.dp)) {
                    // Avatar
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                                    ), CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name field
                    if (isEditingName) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Name") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    saveToFirestore("name", name)
                                    isEditingName = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name.ifEmpty { "Add Name" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { isEditingName = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Bio field
                    if (isEditingBio) {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Bio") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    saveToFirestore("bio", bio)
                                    isEditingBio = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(bio.ifEmpty { "Add a bio..." }, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { isEditingBio = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Bio", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Referral code box
                    Text("Your Referral Code", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4A00E0).copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = referralCode.ifEmpty { "Loading..." },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A00E0),
                            fontSize = 18.sp,
                            letterSpacing = 3.sp
                        )
                        IconButton(onClick = {
                            clipboard.setText(androidx.compose.ui.text.AnnotatedString(referralCode))
                            scope.launch { snackbarHostState.showSnackbar("Referral code copied!") }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF4A00E0))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    DropdownMenuItem(
                        text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChips() {
    var selected by remember { mutableStateOf("All") }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("All", "Unread", "Archived")) { label ->
            FilterChip(
                selected = selected == label,
                onClick = { selected = label },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4A00E0),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
            label = { Text("Chats") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4A00E0),
                selectedTextColor = Color(0xFF4A00E0),
                indicatorColor = Color(0xFF4A00E0).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Contacts") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Call, null) },
            label = { Text("Calls") }
        )
    }
}

fun saveToFirestore(field: String, value: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance().collection("users").document(uid).update(field, value)
}
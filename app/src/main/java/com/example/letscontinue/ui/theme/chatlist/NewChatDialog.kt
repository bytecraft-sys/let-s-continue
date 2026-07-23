package com.example.letscontinue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.letscontinue.ui.theme.User
import com.example.letscontinue.ui.theme.chatlist.ProfileAvatar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onChatStarted: (String) -> Unit
) {
    var referralInput by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun searchUser() {
        val code = referralInput.trim().uppercase()
        if (code.isEmpty()) { errorMessage = "Please enter a referral code"; return }
        if (code.length != 6) { errorMessage = "Referral code must be 6 characters"; return }

        isSearching = true
        errorMessage = ""
        searchResult = null
        focusManager.clearFocus()

        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("referralCode", code)
            .get()
            .addOnSuccessListener { documents ->
                isSearching = false
                if (documents.isEmpty) {
                    errorMessage = "No user found with this code"
                } else {
                    val doc = documents.documents[0]
                    if (doc.id == currentUid) {
                        errorMessage = "You can't chat with yourself!"
                    } else {
                        searchResult = User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "User",
                            email = doc.getString("email") ?: "",
                            photoUrl = doc.getString("photoUrl")
                        )
                    }
                }
            }
            .addOnFailureListener {
                isSearching = false
                errorMessage = "Something went wrong, try again"
            }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor =  MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Start New Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1A2E))
                Text("Enter someone's referral code", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))

                // Input
                OutlinedTextField(
                    value = referralInput,
                    onValueChange = {
                        referralInput = it.uppercase()
                        errorMessage = ""
                        searchResult = null
                    },
                    label = { Text("Referral Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { searchUser() }),
                    trailingIcon = {
                        if (referralInput.isNotEmpty()) {
                            IconButton(onClick = { referralInput = ""; searchResult = null; errorMessage = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor =  MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search button
                Button(
                    onClick = { searchUser() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Error
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Result card
                searchResult?.let { user ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background (MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileAvatar(photoUrl = user.photoUrl, name = user.name, size = 48, fontSize = 18)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(user.email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            saveConnection(
                                currentUid = currentUid,
                                otherUid = user.uid,
                                onSuccess = {
                                    onDismiss()
                                    onChatStarted(user.uid)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor =  MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Chatting", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
            }
        }
    }
}

fun saveConnection(currentUid: String, otherUid: String, onSuccess: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(currentUid)
        .collection("connections").document(otherUid)
        .set(hashMapOf("uid" to otherUid))
        .addOnSuccessListener {
            db.collection("users").document(otherUid)
                .collection("connections").document(currentUid)
                .set(hashMapOf("uid" to currentUid))
                .addOnSuccessListener { onSuccess() }
        }
}

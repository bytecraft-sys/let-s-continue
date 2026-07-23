package com.example.letscontinue.ui.theme.chatlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letscontinue.screen.saveToFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.qrcode.encoder.QRCode
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

@Composable
fun TopSection(
    onLogout: () -> Unit,
    referralCode: String,
    onQrClick: () -> Unit,
    onToggleTheme: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingBio by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploadingPhoto = true
            uploadProfilePhotoBase64(
                uri = it,
                context = context,
                onSuccess = { dataUrl ->
                    photoUrl = dataUrl
                    isUploadingPhoto = false
                    scope.launch { snackbarHostState.showSnackbar("Photo updated") }
                },

                onFailure = {
                    isUploadingPhoto = false
                    scope.launch { snackbarHostState.showSnackbar("Failed to update photo") }
                }

            )
        }
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                bio = document.getString("bio") ?: ""
                photoUrl = document.getString("photoUrl")

            }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Lets Continue",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = name.ifEmpty { "You" },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically,) {
            IconButton(onClick = onQrClick) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = "QR Connect",
                    tint = MaterialTheme.colorScheme.primary
                )

            }
            Box() {

                IconButton(onClick = { expanded = true }) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    )

                    {
                        ProfileAvatar(
                            photoUrl = photoUrl,
                            name = name,
                            size = 40,
                            fontSize = 16
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
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    ProfileAvatar(
                                        photoUrl = photoUrl,
                                        name = name,
                                        size = 90,
                                        fontSize = 32
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
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
                                Text(
                                    name.ifEmpty { "Add Name" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { isEditingName = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Name",
                                        modifier = Modifier.size(18.dp)
                                    )
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
                                Text(
                                    bio.ifEmpty { "Add a bio..." },
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isEditingBio = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Bio",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Referral code box
                        Text(
                            "Your Referral Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = referralCode.ifEmpty { "Loading..." },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                letterSpacing = 3.sp
                            )
                            IconButton(onClick = {
                                clipboard.setText(
                                    androidx.compose.ui.text.AnnotatedString(
                                        referralCode
                                    )
                                )
                                scope.launch { snackbarHostState.showSnackbar("Referral code copied!") }
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        DropdownMenuItem(
                            text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                onLogout()
                            }
                        )
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                Icons.Default.Contrast,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }

}
package com.example.letscontinue

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letscontinue.ui.theme.AuthBackground
import com.example.letscontinue.ui.theme.User
import com.example.letscontinue.ui.theme.chatlist.ProfileAvatar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QRScreen(
    onBack: () -> Unit,
    onUserFound: (String) -> Unit  // opens chat with found userId
) {
    var myReferralCode by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scanResult by remember { mutableStateOf("") }
    var foundUser by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // QR Scanner launcher
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult = result.contents
            isSearching = true
            errorMessage = ""
            foundUser = null

            // Search for user with this referral code
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("referralCode", result.contents.trim().uppercase())
                .get()
                .addOnSuccessListener { documents ->
                    isSearching = false
                    if (documents.isEmpty) {
                        errorMessage = "No user found with this QR code"
                    } else {
                        val doc = documents.documents[0]
                        if (doc.id == currentUid) {
                            errorMessage = "That's your own QR code!"
                        } else {
                            foundUser = User(
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
                    errorMessage = "Scan failed, try again"
                }
        }
    }

    // Load my referral code and generate QR
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUid)
            .get()
            .addOnSuccessListener { doc ->
                val code = doc.getString("referralCode") ?: ""
                myReferralCode = code
                if (code.isNotEmpty()) {
                    qrBitmap = generateQRCode(code)
                }
            }
    }

    AuthBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf( MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            "QR Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Your QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    "Let others scan this to connect with you",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // QR Code display
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(200.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color(0xFFF0EEF8), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color =  MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Referral code text below QR
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = myReferralCode,
                                fontWeight = FontWeight.Bold,
                                color =  MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp,
                                letterSpacing = 4.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Scan Someone's Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    "Point your camera at their QR code",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scan button
                Button(
                    onClick = {
                        val options = ScanOptions().apply {
                            setPrompt("Scan a referral QR code")
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                            setCameraId(0)
                        }
                        scanLauncher.launch(options)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Open Scanner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Loading
                if (isSearching) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color =  MaterialTheme.colorScheme.primary)
                }

                // Error
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Found user card
                foundUser?.let { user ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf( MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary)
                                        ), CircleShape
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

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(user.email, color = Color.Gray, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = {
                                saveConnection(
                                    currentUid = currentUid,
                                    otherUid = user.uid,
                                    onSuccess = {
                                        onBack()
                                        onUserFound(user.uid)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Start Chatting", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Generate QR bitmap from text
fun generateQRCode(text: String): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, 512, 512
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

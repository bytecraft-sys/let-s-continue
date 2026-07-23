package com.example.letscontinue

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.letscontinue.navigation.AppNavGraph
import com.example.letscontinue.ui.theme.LetsContinueTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {

            var isDarkTheme by remember { mutableStateOf(false) }

            LetsContinueTheme (darkTheme = isDarkTheme){
                val navController = rememberNavController()
                AppNavGraph(navController = navController,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )

            }
        }
    }
    override fun onResume() {
        super.onResume()
        // User is active
        setOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        // User left app
        setOnlineStatus(false)
    }

    private fun setOnlineStatus(online: Boolean) {
        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid ?: return
        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update(
                mapOf(
                    "online" to online,
                    "lastSeen" to com.google.firebase.Timestamp.now()
                )
            )
    }
}
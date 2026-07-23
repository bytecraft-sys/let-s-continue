package com.example.letscontinue.ui.theme.chatlist

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.graphics.scale

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    name: String,
    size: Int,
    fontSize: Int
) {
    if (photoUrl != null && photoUrl.startsWith("data:image")) {
        val base64 = photoUrl.substringAfter("base64,")
        val bytes = android.util.Base64.decode(base64,android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if(bitmap!=null){
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile picture",
                modifier = Modifier.size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }else if (photoUrl!= null && photoUrl.startsWith("http")){
         AsyncImage(model = photoUrl,
             contentDescription = "Profile Photo",
             modifier = Modifier
                 .size(size.dp)
                 .clip(CircleShape),
             contentScale = ContentScale.Crop
         )
    }else{
        Text(text = name.firstOrNull()?.uppercase()?:"U",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize= fontSize.sp
        )
    }
}
fun uploadProfilePhotoBase64(
    uri: Uri,
    context: android.content.Context,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        val resized = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val outputStream = java.io.ByteArrayOutputStream()
        resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
        val base64String = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
        val dataUrl = "data:image/jpeg;base64,$base64String"
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("photoUrl", dataUrl)
            .addOnSuccessListener { onSuccess(dataUrl) }
            .addOnFailureListener { onFailure() }
    } catch (e: Exception) {
        onFailure()
    }
}

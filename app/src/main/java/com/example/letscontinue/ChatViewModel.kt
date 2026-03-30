package com.example.letscontinue

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _otherUserName = MutableStateFlow("")
    val otherUserName: StateFlow<String> = _otherUserName

    private val _otherUserOnline = MutableStateFlow(false)
    val otherUserOnline: StateFlow<Boolean> = _otherUserOnline

    fun loadChat(otherUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        // Load other user's name
        db.collection("users").document(otherUserId)
            .addSnapshotListener { doc, _ ->
                _otherUserName.value = doc?.getString("name") ?: "User"
                _otherUserOnline.value = doc?.getBoolean("online") ?: false
            }

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val msgs = snapshot?.documents?.map { doc ->
                    Message(
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                } ?: emptyList()
                _messages.value = msgs
            }
    }

    fun sendMessage(otherUserId: String, text: String) {
        val myUid = auth.currentUser?.uid ?: return
        if (text.isBlank()) return

        val chatId = getChatId(myUid, otherUserId)
        val message = hashMapOf(
            "senderId" to myUid,
            "text" to text.trim(),
            "timestamp" to Timestamp.now()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)

        db.collection("chats").document(chatId).set(
            hashMapOf(
                "participants" to listOf(myUid, otherUserId),
                "lastMessage" to text.trim(),
                "lastTimestamp" to Timestamp.now()
            )
        )
    }

    fun setOnline(online: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("online", online)
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}
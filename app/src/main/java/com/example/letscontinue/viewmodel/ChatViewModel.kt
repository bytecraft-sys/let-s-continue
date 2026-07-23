package com.example.letscontinue.viewmodel

import androidx.lifecycle.ViewModel
import com.example.letscontinue.ui.theme.Message
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

    private val _isOtherTyping = MutableStateFlow(false)
    val isOtherTyping: StateFlow<Boolean> = _isOtherTyping

    fun loadChat(otherUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        // Real-time online status
        db.collection("users").document(otherUserId)
            .addSnapshotListener { doc, _ ->
                _otherUserName.value = doc?.getString("name") ?: "User"
                _otherUserOnline.value = doc?.getBoolean("online") ?: false
            }

        // Real-time messages
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val myUid = auth.currentUser?.uid ?: return@addSnapshotListener
                _messages.value = snapshot?.documents
                    // Step 1 — skip messages deleted for ME
                    ?.filter { doc ->
                        val deletedFor = doc.get("deletedFor") as? List<*> ?: emptyList<String>()
                        !deletedFor.contains(myUid)  // hide if my uid is in deletedFor list
                    }
                    // Step 2 — map to Message objects
                    ?.map { doc ->
                        val isDeletedForEveryone = doc.getBoolean("deletedForEveryone") ?: false
                        Message(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            // if deleted for everyone show this text instead
                            text = if (isDeletedForEveryone) "🚫 This message was deleted"
                            else doc.getString("text") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            status = doc.getString("status") ?: "sent",
                            deletedForEveryone = isDeletedForEveryone
                        )
                    } ?: emptyList()

                markMessagesAsRead(otherUserId)
            }
    }

    fun sendMessage(otherUserId: String, text: String) {
        val myUid = auth.currentUser?.uid ?: return
        if (text.isBlank()) return

        val chatId = getChatId(myUid, otherUserId)

        // Save message
        val message = hashMapOf(
            "senderId" to myUid,
            "text" to text.trim(),
            "timestamp" to Timestamp.now(),
            "status" to "read"
        )
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)

        // Update last message
        db.collection("chats").document(chatId).set(
            hashMapOf(
                "participants" to listOf(myUid, otherUserId),
                "lastMessage" to text.trim(),
                "lastTimestamp" to Timestamp.now()
            )
        )


        // Get sender name and receiver token then send notification
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { senderDoc ->
                val senderName = senderDoc.getString("name") ?: "Someone"

                db.collection("users").document(otherUserId).get()
                    .addOnSuccessListener { receiverDoc ->
                        val token = receiverDoc.getString("fcmToken") ?: return@addOnSuccessListener
                        sendPushNotification(
                            token = token,
                            title = senderName,
                            body = text.trim()
                        )
                    }
            }
    }

    fun setOnline(online: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("online", online)
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun setTyping(otherUserId: String, isTyping: Boolean) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)
        db.collection("chats").document(chatId)
            .update("isTyping", isTyping)
    }

    fun listenForTyping(otherUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)
        db.collection("chats").document(chatId)
            .addSnapshotListener { doc, _ ->
                _isOtherTyping.value = doc?.getBoolean("isTyping") ?: false
            }
    }

    private fun sendPushNotification(token: String, title: String, body: String) {
        // Save notification request to Firestore
        // Firebase Cloud Functions will pick this up and send the FCM
        db.collection("notifications").add(
            hashMapOf(
                "to" to token,
                "title" to title,
                "body" to body,
                "timestamp" to Timestamp.now()
            )
        )
    }

    fun markMessagesAsRead(otherUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("senderId", otherUserId)
            .whereEqualTo("status", "read")
            .get()
            .addOnSuccessListener { document ->
                val batch = db.batch()
                document.forEach { doc ->
                    batch.update(doc.reference, "status", "read")
                }
                batch.commit()
            }
    }

    fun markAsDelivered(otherUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("senderId", otherUserId)
            .whereEqualTo("status", "sent")
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { doc ->
                    batch.update(doc.reference, "status", "delivered")
                }
                batch.commit()
            }
    }

    fun deleteForMe(otherUserId: String, messageId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        // Add my UID to deletedFor list
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update("deletedFor", com.google.firebase.firestore.FieldValue.arrayUnion(myUid))
    }

    fun deleteForEveryone(otherUserId: String, messageId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = getChatId(myUid, otherUserId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "deletedForEveryone" to true,
                    "text" to ""   // clear text
                )
            )
    }
}
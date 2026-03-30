package com.example.letscontinue

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _signupSuccess = MutableStateFlow(false)
    val signupSuccess: StateFlow<Boolean> = _signupSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setError(message: String){
        _error.value=message
    }
    private fun generateReferralCode(): String{
        val char="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { char.random() }
            .joinToString ("")
    }

    fun signup(email: String, password: String, name: String, enteredCode: String) {

        _isLoading.value = true
        _error.value = null

        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanName = name.trim()
        val referralCode = generateReferralCode()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _error.value = "Enter valid email"
            _isLoading.value = false
            return
        }

        if (cleanPassword.length < 6) {
            _error.value = "Password must be at least 6 characters"
            _isLoading.value = false
            return
        }

        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = hashMapOf(
                    "uid" to uid,
                    "name" to cleanName,
                    "email" to cleanEmail,
                    "referralCode" to referralCode,
                    "referredBy" to null,
                    "bio" to ""
                )
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        if (enteredCode.isNotEmpty()) {
                            db.collection("users")
                                .whereEqualTo("referralCode", enteredCode.trim().uppercase())
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        val referrerId = documents.documents[0].id
                                        db.collection("users").document(uid)
                                            .update("referredBy", referrerId)
                                    }
                                    _isLoading.value = false
                                    _signupSuccess.value = true
                                }
                                .addOnFailureListener {
                                    _isLoading.value = false
                                    _error.value = it.localizedMessage
                                }
                        } else {
                            _isLoading.value = false
                            _signupSuccess.value = true
                        }
                    }
                    .addOnFailureListener {
                        _isLoading.value = false
                        _error.value = it.localizedMessage
                    }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _error.value = it.localizedMessage
            }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        _error.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _error.value = task.exception?.message
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    fun logout(){
        auth.signOut()
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
    fun resetSignupSuccess(){
        _signupSuccess.value=false
    }

}
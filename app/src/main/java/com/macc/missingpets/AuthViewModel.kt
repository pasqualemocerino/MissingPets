package com.macc.missingpets

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var isAuthenticated by mutableStateOf(false)

    init {
        viewModelScope.launch {
            auth.currentUser?.let {
                isAuthenticated = true
            }
        }
    }

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun userId(): String {
        return auth.currentUser?.uid.toString()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isAuthenticated = true
                    }
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //isAuthenticated = true

                        val user = auth.currentUser
                        if (user != null) {
                            // Set the username
                            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                // .setPhotoUri(Uri.parse("your_photo_url_here"))
                                .build()
                            user.updateProfile(userProfileChangeRequest)
                        }
                        // else {Exception}
                    }
                    else {
                        // Handle authentication failure
                        isAuthenticated = false
                        val exception = task.exception
                        Log.e("AuthViewModel", "Authentication failed: ${exception?.message}")
                    }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            isAuthenticated = false
        }
    }
}
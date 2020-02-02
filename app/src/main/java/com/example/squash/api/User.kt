package com.example.squash.api

import android.content.ContentValues.TAG
import com.example.squash.MainActivity

import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.coroutines.coroutineContext

class User(private val auth: FirebaseAuth, func: (Boolean, User) -> Unit) {
    companion object {
        var user: FirebaseUser? = null
    }
    init {
        val currentUser = auth.currentUser
        if(currentUser==null) {
            auth.signInAnonymously()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    user = auth.currentUser
                    func(true, this)
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    func(false, this)
                }
                // ...
            }
        } else {
            user = auth.currentUser
            func(true, this)
        }
    }
    fun getUid(): String? {
        return user?.uid
    }
}

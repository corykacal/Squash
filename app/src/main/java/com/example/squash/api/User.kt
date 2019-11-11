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

class User(private val auth: FirebaseAuth) {
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
                    Log.d("###########", "signInAnonymously:success")
                    user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("###########", "signInAnonymously:failure", it.exception)
                }
                // ...
            }
        } else {
            Log.d("##########", "user signed in already")
            user = auth.currentUser
        }
    }
    fun getUid(): String? {
        return user?.uid
    }
}

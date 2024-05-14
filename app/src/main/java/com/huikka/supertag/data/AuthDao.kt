package com.huikka.supertag.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.IOException

class AuthDao {

    private var auth: FirebaseAuth = Firebase.auth

    val user: FirebaseUser? = auth.currentUser

    val isLoggedIn: Boolean = user != null

    suspend fun login(email: String, password: String): Error? {
        var err: Error? = null
        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("LOGIN", task.exception.toString())
                    err = Error(IOException("Error logging in", task.exception))
                }
            }.await()
        } catch (e: Throwable) {
            err = Error(IOException("Error logging in", e))
        }

        return err
    }

    fun logout(): Error? {
        try {
            auth.signOut()
        } catch (e: Throwable) {
            return Error(IOException("Error logging out", e))
        }
        return null
    }

    suspend fun register(email: String, password: String, name: String): Error? {
        var err: Error? = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdate = userProfileChangeRequest {
                        displayName = name
                    }
                    user?.updateProfile(profileUpdate)
                } else {
                    err = Error(IOException("Error registering", task.exception))
                }
            }.await()
        return err
    }
}
package com.huikka.supertag.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private var auth: FirebaseAuth = Firebase.auth

    suspend fun login(email: String, password: String): Error? {
        var err: Error? = null
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
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

    fun getCurrentUser() : FirebaseUser? {
        return auth.currentUser
    }

    suspend fun register(email: String, password: String, displayName: String): Error? {
        var err : Error? = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    err = Error(IOException("Error registering", task.exception))
                }
            }.await()
        return err
    }
}
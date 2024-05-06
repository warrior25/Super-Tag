package com.huikka.supertag.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huikka.supertag.data.model.LoggedInUser
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private var auth: FirebaseAuth = Firebase.auth

    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        var result: Result<LoggedInUser>? = null

        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.uid ?: ""
                        val displayName = auth.currentUser?.displayName ?: ""
                        val user = LoggedInUser(uid, displayName)
                        result = Result.Success(user)
                    } else {
                        Log.e("LOGIN", task.exception.toString())
                        result = Result.Error(IOException("Error logging in", task.exception))
                    }
                }.await()
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }

        return result!!
    }

    fun logout() {
        auth.signOut()
    }
}
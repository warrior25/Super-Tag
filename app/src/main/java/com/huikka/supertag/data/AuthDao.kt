package com.huikka.supertag.data

import com.huikka.supertag.STApplication
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException

class AuthDao(application: STApplication) {

    private var auth: Auth = application.supabase.auth

    val user: UserInfo? = auth.currentUserOrNull()

    val isLoggedIn: Boolean = user != null

    suspend fun login(email: String, password: String): Error? {
        var err: Error? = null
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: Exception) {
            err = Error(IOException("Error logging in", e))
        }
        return err
    }

    suspend fun logout(): Error? {
        try {
            auth.signOut()
        } catch (e: Exception) {
            return Error(IOException("Error logging out", e))
        }
        return null
    }

    suspend fun register(email: String, password: String, nickname: String): Error? {
        var err: Error? = null
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("nickname", nickname)
                }
            }
        } catch (e: Exception) {
            err = Error(IOException("Error registering", e))
        }
        return err
    }
}
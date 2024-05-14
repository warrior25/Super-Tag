package com.huikka.supertag.data

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.PlayerDto
import com.huikka.supertag.data.model.Player
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthDao(application: STApplication) {

    private var auth: Auth = application.supabase.auth
    private var db: Postgrest = application.supabase.postgrest

    suspend fun awaitCurrentSession(): UserSession? {
        auth.awaitInitialization()
        return auth.currentSessionOrNull()
    }

    suspend fun getUser(): UserInfo? {
        return auth.currentUserOrNull()
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(email: String, password: String, nickname: String): Boolean {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("nickname", nickname)
                }
            }
            addPlayerToDatabase()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun addPlayerToDatabase() {
        val user = getUser()
        val player = Player(id = user!!.id)
        val playerDto = PlayerDto(
            id = player.id,
            latitude = player.latitude,
            longitude = player.longitude,
            locationAccuracy = player.locationAccuracy,
            speed = player.speed,
            bearing = player.bearing,
            icon = player.icon,
            gameId = player.gameId,
            isHost = player.isHost
        )
        db.from("players").insert(playerDto)
    }
}
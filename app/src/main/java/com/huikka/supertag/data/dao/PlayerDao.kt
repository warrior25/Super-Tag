package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Player
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow

class PlayerDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    @OptIn(SupabaseExperimental::class)
    fun getPlayersByGameIdFlow(gameId: String): Flow<List<Player>> {
        return db.from("players").selectAsFlow(
            Player::id, filter = FilterOperation("game_id", FilterOperator.EQ, gameId)
        )
    }

    suspend fun updatePlayerLocation(
        id: String,
        latitude: Double,
        longitude: Double,
        locationAccuracy: Float,
        speed: Float,
        bearing: Float,
        zoneId: Int?
    ): Error? {
        try {
            db.from("players").update({
                set("latitude", latitude)
                set("longitude", longitude)
                set("location_accuracy", locationAccuracy)
                set("speed", speed)
                set("bearing", bearing)
                set("zone_id", zoneId)
            }) {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun addToGame(playerId: String, gameId: String, isHost: Boolean = false): Error? {
        try {
            db.from("players").update({
                set("game_id", gameId)
                set("is_host", isHost)
            }) {
                filter {
                    eq("id", playerId)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun removeFromGame(id: String): Error? {
        try {
            db.from("players").update({ setToNull("game_id") }) {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun getPlayerById(id: String): Player? {
        return db.from("players").select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<Player>()
    }

    suspend fun getPlayerLocation(id: String): Player {
        return db.from("players").select(
            columns = (Columns.list(
                "latitude", "longitude", "location_accuracy", "speed", "bearing"
            ))
        ) {
            filter { eq("id", id) }
        }.decodeSingle<Player>()
    }
}
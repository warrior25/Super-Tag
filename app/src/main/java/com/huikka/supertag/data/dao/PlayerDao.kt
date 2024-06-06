package com.huikka.supertag.data.dao

import android.util.Log
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Player
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow
import java.sql.SQLException

class PlayerDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    @OptIn(SupabaseExperimental::class)
    fun getPlayersByGameIdFlow(gameId: String): Flow<List<Player>> {
        return db.from("players").selectAsFlow(
            Player::id, filter = FilterOperation("game_id", FilterOperator.EQ, gameId)
        )
    }

    @OptIn(SupabaseExperimental::class)
    fun getPlayerByIdFlow(id: String): Flow<Player> {
        return db.from("players").selectSingleValueAsFlow(
            Player::id
        ) {
            eq("id", id)
        }
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
        return try {
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
            null
        } catch (e: SQLException) {
            Log.e("LOCATION", "SQL error updating player location for $id: ${e.message}", e)
            Error(e)
        } catch (e: Exception) {
            Log.d("LOCATION", "Error updating player location for $id: $e")
            Error(e)
        }
    }

    suspend fun addToGame(playerId: String, gameId: String, isHost: Boolean = false) {
        db.from("players").update({
            set("game_id", gameId)
            set("is_host", isHost)
        }) {
            filter {
                eq("id", playerId)
            }
        }
    }

    suspend fun removeFromGame(id: String) {
        db.from("players").update({ setToNull("game_id") }) {
            filter {
                eq("id", id)
            }
        }
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
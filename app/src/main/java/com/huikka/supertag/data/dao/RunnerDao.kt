package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Runner
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PrimaryKey
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow

class RunnerDao(application: STApplication) {

    private val db: Postgrest = application.supabase.postgrest
    suspend fun addGame(gameId: String) {
        db.from("runners").insert(Runner(gameId))
    }

    suspend fun setLocation(
        gameId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        lastUpdate: Long,
        nextUpdate: Long
    ) {
        db.from("runners").update({
            set("latitude", latitude)
            set("longitude", longitude)
            set("location_accuracy", accuracy)
            set("last_update", lastUpdate)
            set("next_update", nextUpdate)
        }) {
            filter {
                eq("game_id", gameId)
            }
        }
    }

    suspend fun getNextUpdateTime(gameId: String): Long? {
        return db.from("runners").select(columns = Columns.list("next_update")) {
            filter {
                eq("game_id", gameId)
            }
        }.decodeSingle<Runner>().nextUpdate
    }

    suspend fun setNextUpdateTime(gameId: String, nextUpdate: Long) {
        db.from("runners").update({
            set("next_update", nextUpdate)
        }) {
            filter {
                eq("game_id", gameId)
            }
        }
    }

    suspend fun getLastUpdateTime(gameId: String): Long? {
        return db.from("runners").select(columns = Columns.list("last_update")) {
            filter {
                eq("game_id", gameId)
            }
        }.decodeSingle<Runner>().lastUpdate
    }

    @OptIn(SupabaseExperimental::class)
    fun getRunnerFlow(gameId: String): Flow<Runner> {
        return db.from("runners").selectSingleValueAsFlow(PrimaryKey("game_id") { it.gameId!! }) {
            eq("game_id", gameId)
        }
    }
}
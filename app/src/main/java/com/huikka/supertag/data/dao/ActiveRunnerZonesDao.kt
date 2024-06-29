package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.ActiveRunnerZones
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PrimaryKey
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow

class ActiveRunnerZonesDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    suspend fun setActiveRunnerZones(gameId: String, zones: List<Int>, nextUpdate: Long) {
        db.from("active_runner_zones").update({
            set("active_zone_ids", zones)
            set("next_update", nextUpdate)
        }) {
            filter {
                eq("game_id", gameId)
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun getActiveRunnerZonesFlow(gameId: String): Flow<ActiveRunnerZones> {
        return db.from("active_runner_zones")
            .selectSingleValueAsFlow(PrimaryKey("game_id") { it.gameId }) {
                eq("game_id", gameId)
            }
    }

    suspend fun addGame(newId: String) {
        db.from("active_runner_zones").insert(ActiveRunnerZones(gameId = newId))
    }
}
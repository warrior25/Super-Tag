package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Player
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow

class PlayerDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    @OptIn(SupabaseExperimental::class)
    fun getPlayersByGameIdFlow(gameId: String): Flow<List<Player>> {
        return db.from("players").selectAsFlow(
            Player::id, filter = FilterOperation("gameId", FilterOperator.EQ, gameId)
        )
    }
}
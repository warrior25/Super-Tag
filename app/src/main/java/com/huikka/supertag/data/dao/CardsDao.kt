package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Card
import com.huikka.supertag.data.helpers.cards
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PrimaryKey
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow

class CardsDao(application: STApplication) {

    private val db: Postgrest = application.supabase.postgrest

    suspend fun addGame(gameId: String) {
        db.from("cards").insert(Card(gameId, List(cards.size) { null }))
    }

    suspend fun updateCardsStatus(gameId: String, status: List<Long?>) {
        db.from("cards").update({
            set("cards_active_until", status)
        }) {
            filter {
                eq("game_id", gameId)
            }
        }
    }

    suspend fun getCardsStatus(gameId: String): Card {
        return db.from("cards").select(columns = Columns.list("cards_active_until")) {
            filter {
                eq("game_id", gameId)
            }
        }.decodeSingle<Card>()
    }

    @OptIn(SupabaseExperimental::class)
    fun getCardsStatusFlow(gameId: String): Flow<Card> {
        return db.from("cards").selectSingleValueAsFlow(PrimaryKey("game_id") { it.gameId }) {
            eq("game_id", gameId)
        }
    }
}
package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Card
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class CardsDao(application: STApplication) {

    private val db: Postgrest = application.supabase.postgrest

    suspend fun addGame(gameId: String) {
        db.from("cards").insert(Card(gameId, listOf(null)))
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
}
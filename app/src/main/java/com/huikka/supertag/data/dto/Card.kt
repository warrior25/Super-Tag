package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    @SerialName("game_id") val gameId: String = "",
    @SerialName("cards_active_until") val cardsActiveUntil: List<Long?>? = null,
)
package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentGame(

    @SerialName("game_id") val gameId: String?, @SerialName("is_host") val isHost: Boolean
)

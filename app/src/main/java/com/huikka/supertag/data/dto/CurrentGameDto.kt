package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentGameDto(

    @SerialName("gameId") val gameId: String?, @SerialName("isHost") val isHost: Boolean
)

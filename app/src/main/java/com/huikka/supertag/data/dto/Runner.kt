package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Runner(
    @SerialName("game_id") val gameId: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("next_update") val nextUpdate: String? = null,
    @SerialName("last_update") val lastUpdate: String? = null,
)
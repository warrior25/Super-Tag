package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Player(

    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("location_accuracy") val locationAccuracy: Float? = null,
    @SerialName("speed") val speed: Float? = null,
    @SerialName("bearing") val bearing: Float? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("game_id") val gameId: String? = null,
    @SerialName("is_host") val isHost: Boolean? = null,
    @SerialName("zone_id") val zoneId: Int? = null
)

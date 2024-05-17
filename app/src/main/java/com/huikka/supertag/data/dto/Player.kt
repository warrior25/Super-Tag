package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Player(

    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("locationAccuracy") val locationAccuracy: Float? = null,
    @SerialName("speed") val speed: Float? = null,
    @SerialName("bearing") val bearing: Float? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("gameId") val gameId: String? = null,
    @SerialName("isHost") val isHost: Boolean? = null
)

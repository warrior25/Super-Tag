package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDto(

    @SerialName("id") val id: String,
    @SerialName("latitude") val latitude: Float?,
    @SerialName("longitude") val longitude: Float?,
    @SerialName("locationAccuracy") val locationAccuracy: Float?,
    @SerialName("speed") val speed: Float?,
    @SerialName("bearing") val bearing: Float?,
    @SerialName("icon") val icon: String,
    @SerialName("gameId") val gameId: String?,
    @SerialName("isHost") val isHost: Boolean
)

package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Zone(

    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0,
    @SerialName("radius") val radius: Int = 0,
    @SerialName("drawable") val drawable: String = ""
)

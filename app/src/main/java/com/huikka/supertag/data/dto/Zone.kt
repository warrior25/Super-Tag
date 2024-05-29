package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Zone(

    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("radius") val radius: Int? = null,
    @SerialName("drawable") val drawable: String? = null
)

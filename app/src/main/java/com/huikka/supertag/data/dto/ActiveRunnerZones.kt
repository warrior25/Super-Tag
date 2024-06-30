package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveRunnerZones(

    @SerialName("game_id") val gameId: String = "",
    @SerialName("active_zone_ids") val activeZoneIds: List<Int>? = null,
    @SerialName("next_update") val nextUpdate: Long? = null,
)
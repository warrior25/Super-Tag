package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(

    @SerialName("id") val id: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("runner_id") val runnerId: String? = null,
    @SerialName("runner_money") val runnerMoney: Int? = null,
    @SerialName("chaser_money") val chaserMoney: Int? = null,
    @SerialName("robbery_in_progress") val robberyInProgress: Boolean? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("head_start") val headStart: Int? = null,
    @SerialName("initial_tracking_interval") val initialTrackingInterval: Int? = null
)
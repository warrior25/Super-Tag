package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(

    @SerialName("id") val id: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("runner_id") val runnerId: String? = null,
    @SerialName("runner_money") val runnerMoney: Int = 0,
    @SerialName("chaser_money") val chaserMoney: Int = 0,
    @SerialName("start_time") val startTime: Long? = null,
    @SerialName("end_time") val endTime: Long? = null,
    @SerialName("head_start") val headStart: Int = 0,
    @SerialName("initial_tracking_interval") val initialTrackingInterval: Int = 0
)
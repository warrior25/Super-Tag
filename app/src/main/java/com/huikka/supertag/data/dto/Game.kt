package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(

    @SerialName("id") val id: String,
    @SerialName("status") val status: String,
    @SerialName("runnerId") val runnerId: String? = null,
    @SerialName("runnerMoney") val runnerMoney: Int? = null,
    @SerialName("chaserMoney") val chaserMoney: Int? = null,
    @SerialName("robberyInProgress") val robberyInProgress: Boolean? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("headStart") val headStart: Int? = null,
    @SerialName("nextRunnerLocationUpdate") val nextRunnerLocationUpdate: String? = null,
    @SerialName("lastRunnerLocationUpdate") val lastRunnerLocationUpdate: String? = null
)
package com.huikka.supertag.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(

    @SerialName("id") val id: String,
    @SerialName("status") val status: String,
    @SerialName("runnerId") val runner: String?,
    @SerialName("runnerMoney") val runnerMoney: Int,
    @SerialName("chaserMoney") val chaserMoney: Int,
    @SerialName("robberyInProgress") val robberyInProgress: Boolean,
    @SerialName("startTime") val startTime: String?,
    @SerialName("endTime") val endTime: String?,
    @SerialName("headStart") val headStart: Int,
    @SerialName("nextRunnerLocationUpdate") val nextRunnerLocationUpdate: String?,
    @SerialName("lastRunnerLocationUpdate") val lastRunnerLocationUpdate: String?
)
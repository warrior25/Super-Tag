package com.huikka.supertag.data.model


data class Game(

    val id: String,
    val status: String = "lobby",
    val runner: String? = null,
    val runnerMoney: Int = 30,
    val chaserMoney: Int = 30,
    val robberyInProgress: Boolean = false,
    val startTime: String? = null,
    val endTime: String? = null,
    val headStart: Int = 10,
    val nextRunnerLocationUpdate: String? = null,
    val lastRunnerLocationUpdate: String? = null
)

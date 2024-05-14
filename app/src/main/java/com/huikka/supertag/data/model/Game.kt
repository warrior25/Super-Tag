package com.huikka.supertag.data.model

import com.google.type.DateTime

data class Game(
    val id: String,
    val status: String = "Lobby",
    val runner: Player? = null,
    val runnerMoney: Int = 30,
    val chaserMoney: Int = 30,
    val robberyInProgress: Boolean = false,
    val chasers: List<Player>,
    val zones: List<Zone>? = null,
    val startTime: DateTime? = null,
    val endTime: DateTime? = null,
    val headStart: Int = 10,
    val nextRunnerLocationUpdate: DateTime? = null,
    val lastRunnerLocationUpdate: DateTime? = null
) {
    // No-arg constructor for deserialization
    constructor() : this(
        id = "",
        chasers = emptyList()
    )
}

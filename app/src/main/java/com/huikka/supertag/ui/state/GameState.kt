package com.huikka.supertag.ui.state

import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.dto.Runner
import com.huikka.supertag.data.dto.Zone

data class GameState(
    val gameId: String? = null,
    val userId: String? = null,
    val playingArea: Zone = Zone(),
    val isInitialized: Boolean = false,
    val isRunner: Boolean = false,
    val runnerZones: List<Zone> = listOf(),
    val chaserZones: List<Zone> = listOf(),
    val activeRunnerZones: List<Zone> = listOf(),
    val zoneUpdateTimer: TimerState = TimerState(),
    val runnerLocationUpdateTimer: TimerState = TimerState(),
    val runner: Runner? = null,
    val runnerName: String = "",
    val runnerId: String = "",
    val players: List<Player> = listOf()
)
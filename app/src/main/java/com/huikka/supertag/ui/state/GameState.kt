package com.huikka.supertag.ui.state

import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.dto.Runner
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.Sides

data class GameState(
    val gameId: String = "",
    val userId: String = "",
    val playingArea: Zone = Zone(),
    val isInitialized: Boolean = false,
    val side: Sides = Sides.Chasers,
    val runnerZones: List<Zone> = listOf(),
    val chaserZones: List<Zone> = listOf(),
    val activeRunnerZones: List<Zone> = listOf(),
    val zoneShuffleTimer: TimerState = TimerState(),
    val runnerLocationUpdateTimer: TimerState = TimerState(),
    val runner: Runner? = null,
    val runnerName: String = "",
    val runnerId: String = "",
    val players: List<Player> = listOf(),
    val money: Int = 0,
    val currentZone: Zone? = null,
    val zonePresenceTimer: TimerState = TimerState()
)
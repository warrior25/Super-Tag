package com.huikka.supertag.ui.state

import com.huikka.supertag.data.dto.Zone

data class GameState(
    val mapLoaded: Boolean = false,
    val playingArea: Zone? = null,
    val isInitialized: Boolean = false,
    val isRunner: Boolean = false,
    val runnerZones: List<Zone> = listOf(),
    val chaserZones: List<Zone> = listOf()
)
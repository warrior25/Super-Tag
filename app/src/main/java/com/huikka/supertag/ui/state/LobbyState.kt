package com.huikka.supertag.ui.state

import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.dto.Player

data class LobbyState(
    val gameId: String = "",
    val players: List<Player> = listOf(),
    val isHost: Boolean = false,
    val game: Game? = null,
    val error: String = "",
    val isInitialized: Boolean = false
)
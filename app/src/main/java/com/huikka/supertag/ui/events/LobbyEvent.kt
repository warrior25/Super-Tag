package com.huikka.supertag.ui.events

sealed class LobbyEvent {
    data object OnStartGameClick : LobbyEvent()
    data class OnRunnerChange(val runnerId: String) : LobbyEvent()
    data object OnLeaveGameClick : LobbyEvent()
    data class OnInit(val gameId: String) : LobbyEvent()
    data object OnNavigateAway : LobbyEvent()
}
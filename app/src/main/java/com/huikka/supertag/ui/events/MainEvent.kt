package com.huikka.supertag.ui.events

sealed class MainEvent {
    data object OnInit : MainEvent()
    data class OnGameIdChange(val gameId: String) : MainEvent()
    data object OnJoinGameClick : MainEvent()
    data object OnHostGameClick : MainEvent()
}
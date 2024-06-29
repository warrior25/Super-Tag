package com.huikka.supertag.ui.events

sealed class GameEvent {
    data object OnInit : GameEvent()
    data object OnLeaveGame : GameEvent()
    data class OnCardActivate(val cardIndex: Int) : GameEvent()
    data class OnCardDeactivate(val cardIndex: Int) : GameEvent()
}
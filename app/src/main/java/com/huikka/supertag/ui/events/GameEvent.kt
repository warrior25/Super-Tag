package com.huikka.supertag.ui.events

sealed class GameEvent {
    data object OnInit : GameEvent()
}
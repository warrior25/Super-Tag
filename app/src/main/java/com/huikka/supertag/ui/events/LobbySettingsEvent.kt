package com.huikka.supertag.ui.events

sealed class LobbySettingsEvent {
    data class OnHeadStartChange(val headStart: Int) : LobbySettingsEvent()
    data class OnRunnerMoneyChange(val runnerCoins: Int) : LobbySettingsEvent()
    data class OnChaserMoneyChange(val chaserCoins: Int) : LobbySettingsEvent()
    data class OnSave(val gameId: String) : LobbySettingsEvent()
    data class OnSettingsReset(val headStart: Int, val runnerMoney: Int, val chaserMoney: Int) :
        LobbySettingsEvent()
}
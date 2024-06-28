package com.huikka.supertag.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.ui.events.LobbySettingsEvent
import com.huikka.supertag.ui.state.LobbySettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbySettingsViewModel(
    private val gameDao: GameDao
) : ViewModel() {
    private val _state = MutableStateFlow(LobbySettingsState())
    val state = _state.asStateFlow()

    fun onEvent(event: LobbySettingsEvent) {
        when (event) {
            is LobbySettingsEvent.OnHeadStartChange -> updateHeadStart(event.headStart)
            is LobbySettingsEvent.OnChaserMoneyChange -> updateChaserMoney(event.chaserCoins)
            is LobbySettingsEvent.OnRunnerMoneyChange -> updateRunnerMoney(event.runnerCoins)
            is LobbySettingsEvent.OnSave -> saveSettings(event.gameId)
            is LobbySettingsEvent.OnSettingsReset -> resetSettings(
                event.headStart, event.chaserMoney, event.runnerMoney
            )
        }
    }

    private fun resetSettings(headStart: Int, chaserMoney: Int, runnerMoney: Int) {
        _state.update {
            it.copy(
                headStart = headStart, chaserMoney = chaserMoney, runnerMoney = runnerMoney
            )
        }
    }

    private fun updateHeadStart(headStart: Int) {
        _state.update {
            it.copy(
                headStart = headStart
            )
        }
    }

    private fun updateChaserMoney(money: Int) {
        _state.update {
            it.copy(
                chaserMoney = money
            )
        }
    }

    private fun updateRunnerMoney(money: Int) {
        _state.update {
            it.copy(
                runnerMoney = money
            )
        }
    }

    private fun saveSettings(gameId: String) {
        // TODO: Add error handling
        viewModelScope.launch(Dispatchers.IO) {
            gameDao.changeSettings(
                gameId = gameId,
                headStart = state.value.headStart,
                chaserMoney = state.value.chaserMoney,
                runnerMoney = state.value.runnerMoney
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val myApp = application as STApplication

                return LobbySettingsViewModel(
                    myApp.gameDao
                ) as T
            }
        }
    }
}
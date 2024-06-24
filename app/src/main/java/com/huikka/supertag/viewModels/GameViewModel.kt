package com.huikka.supertag.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.ui.events.GameEvent
import com.huikka.supertag.ui.state.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val zoneDao: ZoneDao
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OnInit -> initData()
        }
    }

    private fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            getZones()
            _state.update {
                it.copy(
                    playingArea = zoneDao.getPlayingArea(), isInitialized = true
                )
            }
        }
    }

    private suspend fun getZones() {
        val chaserZones = mutableListOf<Zone>()
        val runnerZones = mutableListOf<Zone>()
        val allZones = zoneDao.getZones()
        for (zone in allZones) {
            if (zone.type == ZoneTypes.ATM || zone.type == ZoneTypes.STORE) {
                chaserZones.add(zone)
            } else if (zone.type == ZoneTypes.ATTRACTION) {
                runnerZones.add(zone)
            }
        }
        _state.update {
            it.copy(
                runnerZones = runnerZones, chaserZones = chaserZones
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

                return GameViewModel(
                    myApp.authDao, myApp.gameDao, myApp.playerDao, myApp.zoneDao
                ) as T
            }
        }
    }
}
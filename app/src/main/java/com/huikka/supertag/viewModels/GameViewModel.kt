package com.huikka.supertag.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.ActiveRunnerZonesDao
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.TimeConverter
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.data.helpers.second
import com.huikka.supertag.ui.events.GameEvent
import com.huikka.supertag.ui.state.GameState
import com.huikka.supertag.ui.state.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val zoneDao: ZoneDao,
    private val activeRunnerZonesDao: ActiveRunnerZonesDao
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
            getUserData()
            getZones()
            getIsRunner()
            listenToGameDataChanges()
            _state.update {
                it.copy(
                    playingArea = zoneDao.getPlayingArea(), isInitialized = true
                )
            }
        }
    }

    private suspend fun getUserData() {
        val userId = authDao.getUser()!!.id
        val gameId = gameDao.getCurrentGameInfo(userId).gameId!!
        _state.update {
            it.copy(
                gameId = gameId, userId = userId
            )
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

    private suspend fun getIsRunner() {
        val runnerId = gameDao.getRunnerId(state.value.gameId!!)!!
        _state.update {
            it.copy(
                isRunner = state.value.userId == runnerId
            )
        }
    }

    private fun listenToGameDataChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val flow = gameDao.getGameFlow(state.value.gameId!!)
            flow.collect { game ->
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val flow = activeRunnerZonesDao.getActiveRunnerZonesFlow(state.value.gameId!!)
            flow.collect {
                if (it.activeZoneIds != null) {
                    updateActiveRunnerZones(it.activeZoneIds, it.nextUpdate!!)
                }
            }
        }
    }

    private fun updateActiveRunnerZones(zoneIds: List<Int>, nextUpdate: String) {
        val activeZones = state.value.runnerZones.filter { it.id in zoneIds }
        val delay =
            TimeConverter.timestampToLong(nextUpdate).minus(System.currentTimeMillis() + second)
                .coerceAtLeast(0)

        // Might not restart timer if delay is exactly same as previously,
        // unlikely when working with milliseconds
        _state.update {
            it.copy(
                activeRunnerZones = activeZones, zoneUpdateTimer = TimerState(
                    time = delay
                )
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
                    myApp.authDao,
                    myApp.gameDao,
                    myApp.playerDao,
                    myApp.zoneDao,
                    myApp.activeRunnerZonesDao
                ) as T
            }
        }
    }
}
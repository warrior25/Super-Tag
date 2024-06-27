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
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.dto.Runner
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.ui.events.GameEvent
import com.huikka.supertag.ui.state.GameState
import com.huikka.supertag.ui.state.TimerState
import com.instacart.truetime.time.TrueTime
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
    private val activeRunnerZonesDao: ActiveRunnerZonesDao,
    private val runnerDao: RunnerDao,
    private val trueTime: TrueTime
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OnInit -> initData()
            is GameEvent.OnLeaveGame -> leaveGame()
        }
    }

    private fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            getUserData()
            getZones()
            getRunnerData()
            listenToGameDataChanges()
            _state.update {
                it.copy(
                    playingArea = zoneDao.getPlayingArea()
                )
            }
        }
    }

    private suspend fun getUserData() {
        authDao.awaitCurrentSession()
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

    private suspend fun getRunnerData() {
        val runnerId = gameDao.getRunnerId(state.value.gameId!!)!!
        val runnerName = playerDao.getPlayerById(runnerId)!!.name!!
        _state.update {
            it.copy(
                isRunner = state.value.userId == runnerId,
                runnerName = runnerName,
                runnerId = runnerId
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
            val flow = playerDao.getPlayersByGameIdFlow(state.value.gameId!!)
            flow.collect {
                updatePlayers(it)
            }
        }
        if (state.value.isRunner) {
            viewModelScope.launch(Dispatchers.IO) {
                val flow = activeRunnerZonesDao.getActiveRunnerZonesFlow(state.value.gameId!!)
                flow.collect {
                    if (it.activeZoneIds != null) {
                        updateActiveRunnerZones(it.activeZoneIds, it.nextUpdate!!)
                    }
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val flow = runnerDao.getRunnerFlow(state.value.gameId!!)
                flow.collect {
                    updateRunner(it)
                }
            }
        }
    }

    private fun updatePlayers(players: List<Player>) {
        _state.update {
            it.copy(
                players = players
            )
        }
    }

    private fun updateActiveRunnerZones(zoneIds: List<Int>, nextUpdate: Long) {
        val activeZones = state.value.runnerZones.filter { it.id in zoneIds }
        val delay = nextUpdate.minus(trueTime.now().time).coerceAtLeast(0)

        // Might not restart timer if delay is exactly same as previously,
        // unlikely when working with milliseconds
        _state.update {
            it.copy(
                activeRunnerZones = activeZones, zoneUpdateTimer = TimerState(
                    time = delay
                ), isInitialized = true
            )
        }
    }

    private fun updateRunner(runner: Runner) {
        if (runner.nextUpdate == null) {
            return
        }
        val delay = runner.nextUpdate.minus(trueTime.now().time).coerceAtLeast(0)
        _state.update {
            it.copy(
                runner = runner,
                runnerLocationUpdateTimer = TimerState(time = delay),
                isInitialized = true
            )
        }
    }

    private fun leaveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.isRunner) {
                gameDao.removeGame(state.value.gameId!!)
            } else {
                playerDao.removeFromGame(state.value.userId!!)
            }
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
                    myApp.activeRunnerZonesDao,
                    myApp.runnerDao,
                    myApp.trueTime
                ) as T
            }
        }
    }
}
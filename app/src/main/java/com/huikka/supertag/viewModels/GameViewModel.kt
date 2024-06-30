package com.huikka.supertag.viewModels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.ActiveRunnerZonesDao
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.CardsDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.dto.Runner
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.data.helpers.Sides
import com.huikka.supertag.data.helpers.TimerType
import com.huikka.supertag.data.helpers.ZoneType
import com.huikka.supertag.data.helpers.cards
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
    private val cardsDao: CardsDao,
    private val trueTime: TrueTime
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    private val _cardStates = MutableStateFlow(
        cards
    )
    val cardStates = _cardStates.asStateFlow()

    private val cardActions = listOf { updateLiveRunnerLocation(null) }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OnInit -> initData()
            is GameEvent.OnLeaveGame -> leaveGame()
            is GameEvent.OnCardActivate -> activateCard(event.cardIndex)
            is GameEvent.OnCardDeactivate -> deactivateCard(event.cardIndex)
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
        val gameId = gameDao.getCurrentGameInfo(userId).gameId
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
            if (zone.type == ZoneType.ATM || zone.type == ZoneType.STORE) {
                chaserZones.add(zone)
            } else if (zone.type == ZoneType.ATTRACTION) {
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
        val runnerId = gameDao.getRunnerId(state.value.gameId)!!
        val runnerName = playerDao.getPlayerById(runnerId)!!.name
        val side = if (state.value.userId == runnerId) {
            Sides.Runner
        } else {
            Sides.Chasers
        }
        _state.update {
            it.copy(
                side = side, runnerName = runnerName, runnerId = runnerId
            )
        }
    }

    private fun listenToGameDataChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            val flow = gameDao.getGameFlow(state.value.gameId)
            flow.collect {
                updateGame(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val flow = playerDao.getPlayersByGameIdFlow(state.value.gameId)
            flow.collect {
                updatePlayers(it)
            }
        }
        if (state.value.side == Sides.Runner) {
            viewModelScope.launch(Dispatchers.IO) {
                val flow = activeRunnerZonesDao.getActiveRunnerZonesFlow(state.value.gameId)
                flow.collect {
                    if (it.activeZoneIds != null) {
                        updateActiveRunnerZones(it.activeZoneIds, it.nextUpdate!!)
                    }
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val flow = runnerDao.getRunnerFlow(state.value.gameId)
                flow.collect {
                    updateRunner(it)
                }
            }
        }
    }

    private fun updateGame(game: Game) {
        val money = if (state.value.side == Sides.Runner) {
            game.runnerMoney
        } else {
            game.chaserMoney
        }
        _state.update {
            it.copy(
                money = money
            )
        }
        updateCardStates()
    }

    private fun updateCardStates() {
        viewModelScope.launch(Dispatchers.IO) {
            val status = cardsDao.getCardsStatus(state.value.gameId)

            _cardStates.update { cardStates ->
                cardStates.mapIndexed { index, cardState ->
                    val activeUntil = status.cardsActiveUntil?.get(index)
                    val timeRemaining =
                        activeUntil?.minus(trueTime.now().time)?.coerceAtLeast(0) ?: 0

                    if (timeRemaining > 0L && !cardState.timerState.isRunning) {
                        startCardTimer(index, timeRemaining)
                    }
                    if (activeUntil != cardStates[index].activeUntil) {
                        cardActions[index].invoke()
                    }
                    cardState.copy(
                        enabled = state.value.money >= cardState.cost && timeRemaining == 0L,
                        activeUntil = activeUntil
                    )
                }
            }
        }
    }

    private fun activateCard(cardIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeUntil = trueTime.now().time + cardStates.value[cardIndex].timerState.totalTime
            _cardStates.update { cardStates ->
                cardStates.mapIndexed { index, card ->
                    if (index == cardIndex) {
                        card.copy(
                            enabled = false, activeUntil = activeUntil
                        )
                    } else {
                        card
                    }
                }
            }

            val statusList = cardStates.value.map {
                it.activeUntil
            }
            cardsDao.updateCardsStatus(gameId = state.value.gameId, status = statusList)

            gameDao.reduceMoney(
                gameId = state.value.gameId,
                side = state.value.side,
                amount = cardStates.value[cardIndex].cost
            )

            startCardTimer(cardIndex)
            cardActions[cardIndex].invoke()
        }
    }

    private fun deactivateCard(cardIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _cardStates.update { cardStates ->
                cardStates.mapIndexed { index, card ->
                    if (index == cardIndex) {
                        card.copy(timeRemaining = 0)
                    } else {
                        card
                    }
                }
            }
        }
    }

    private suspend fun updatePlayers(players: List<Player>) {
        val currentPlayer = players.find { it.id == state.value.userId }!!
        var zone: Zone? = null
        if (currentPlayer.zoneId != null) {
            zone = zoneDao.getZoneById(currentPlayer.zoneId)
        }

        _state.update {
            it.copy(
                players = players, currentZone = zone
            )
        }

        Log.d(
            "zonePresenceTimer",
            "${zone?.name}: ${currentPlayer.enteredZone}, ${state.value.zonePresenceTimer.isRunning}"
        )
        if (zone != null && currentPlayer.enteredZone != null) {
            if (!state.value.zonePresenceTimer.isRunning) {
                val (startTime, totalTime) = calculateZonePresenceTime(
                    zone, currentPlayer.enteredZone
                )
                startTimer(
                    timerType = TimerType.ZonePresence, startTime = startTime, totalTime = totalTime
                )
            }
        } else {
            state.value.zonePresenceTimer.timer?.cancel()
            _state.update {
                it.copy(zonePresenceTimer = it.zonePresenceTimer.copy(isRunning = false))
            }
        }

        if (cardStates.value[0].timerState.isRunning) {
            val runner = players.find { it.id == state.value.runnerId }!!
            updateLiveRunnerLocation(runner)
        }
    }

    private fun calculateZonePresenceTime(zone: Zone, enterTime: Long): Pair<Long, Long> {
        val zoneCaptureTime: Long =
            if (state.value.side == Sides.Runner && zone in state.value.activeRunnerZones) {
                when (zone.type) {
                    ZoneType.ATTRACTION -> Config.ATTRACTION_TIME
                    else -> 0
                }
            } else {
                when (zone.type) {
                    ZoneType.ATM -> Config.ATM_TIME
                    ZoneType.STORE -> Config.STORE_TIME
                    else -> 0
                }
            }
        val timeInZone = trueTime.now().time.minus(enterTime)
        return Pair(zoneCaptureTime.minus(timeInZone), zoneCaptureTime)
    }

    private fun updateActiveRunnerZones(zoneIds: List<Int>, nextUpdate: Long) {
        val activeZones = state.value.runnerZones.filter { it.id in zoneIds }
        val delay = nextUpdate.minus(trueTime.now().time).coerceAtLeast(0)

        startTimer(
            timerType = TimerType.ZoneShuffle,
            startTime = delay,
            totalTime = Config.RUNNER_ZONE_SHUFFLE_TIME
        )

        _state.update {
            it.copy(
                activeRunnerZones = activeZones, isInitialized = true
            )
        }
    }

    private fun updateRunner(runner: Runner) {
        if (runner.nextUpdate == null) {
            return
        }
        val delay = runner.nextUpdate.minus(trueTime.now().time).coerceAtLeast(0)
        startTimer(timerType = TimerType.RunnerLocation, startTime = delay)

        _state.update {
            it.copy(
                runner = runner, isInitialized = true
            )
        }
    }

    private fun leaveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.side == Sides.Runner) {
                gameDao.removeGame(state.value.gameId)
            } else {
                playerDao.removeFromGame(state.value.userId)
            }
        }
    }

    private fun updateLiveRunnerLocation(runner: Player? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val runnerLive = runner ?: playerDao.getPlayerById(state.value.runnerId)!!
            _state.update {
                it.copy(
                    runner = state.value.runner?.copy(
                        latitude = runnerLive.latitude,
                        longitude = runnerLive.longitude,
                        locationAccuracy = runnerLive.locationAccuracy
                    )
                )
            }
        }
    }

    private fun startCardTimer(cardIndex: Int, startTime: Long? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            val timerState = cardStates.value[cardIndex].timerState
            timerState.timer?.cancel()

            val cardTimer = object : CountDownTimer(startTime ?: timerState.totalTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _cardStates.update { cardStates ->
                        cardStates.mapIndexed { index, card ->
                            if (index == cardIndex) {
                                card.copy(
                                    timerState = card.timerState.copy(
                                        currentTime = millisUntilFinished, isRunning = true
                                    )
                                )
                            } else {
                                card
                            }
                        }
                    }
                }

                override fun onFinish() {
                    _cardStates.update { cardStates ->
                        cardStates.mapIndexed { index, card ->
                            if (index == cardIndex) {
                                card.copy(
                                    timerState = card.timerState.copy(
                                        isRunning = false
                                    )
                                )
                            } else {
                                card
                            }
                        }
                    }
                }
            }

            _cardStates.update { cardStates ->
                cardStates.mapIndexed { index, card ->
                    if (index == cardIndex) {
                        card.copy(
                            timerState = card.timerState.copy(
                                timer = cardTimer
                            )
                        )
                    } else {
                        card
                    }
                }
            }

            cardTimer.start()
        }
    }

    private fun startTimer(timerType: TimerType, startTime: Long? = null, totalTime: Long? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            val timerState: TimerState = when (timerType) {
                TimerType.ZonePresence -> state.value.zonePresenceTimer
                TimerType.RunnerLocation -> state.value.runnerLocationUpdateTimer
                TimerType.ZoneShuffle -> state.value.zoneShuffleTimer
            }
            timerState.timer?.cancel()

            val cardTimer = object : CountDownTimer(startTime ?: timerState.totalTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    when (timerType) {
                        TimerType.ZonePresence -> _state.update {
                            it.copy(
                                zonePresenceTimer = state.value.zonePresenceTimer.copy(
                                    currentTime = millisUntilFinished,
                                    totalTime = totalTime ?: startTime ?: timerState.totalTime,
                                    isRunning = true
                                )
                            )
                        }

                        TimerType.ZoneShuffle -> _state.update {
                            it.copy(
                                zoneShuffleTimer = state.value.zoneShuffleTimer.copy(
                                    currentTime = millisUntilFinished,
                                    isRunning = true,
                                    totalTime = totalTime ?: startTime ?: timerState.totalTime
                                )
                            )
                        }

                        TimerType.RunnerLocation -> _state.update {
                            it.copy(
                                runnerLocationUpdateTimer = state.value.runnerLocationUpdateTimer.copy(
                                    currentTime = millisUntilFinished,
                                    isRunning = true,
                                    totalTime = totalTime ?: startTime ?: timerState.totalTime
                                )
                            )
                        }
                    }
                }

                override fun onFinish() {
                    when (timerType) {
                        TimerType.ZonePresence -> _state.update {
                            it.copy(zonePresenceTimer = state.value.zonePresenceTimer.copy(isRunning = false))
                        }

                        TimerType.ZoneShuffle -> _state.update {
                            it.copy(zoneShuffleTimer = state.value.zoneShuffleTimer.copy(isRunning = false))
                        }

                        TimerType.RunnerLocation -> _state.update {
                            it.copy(
                                runnerLocationUpdateTimer = state.value.runnerLocationUpdateTimer.copy(
                                    isRunning = false
                                )
                            )
                        }
                    }
                }
            }
            cardTimer.start()

            when (timerType) {
                TimerType.ZonePresence -> _state.update {
                    it.copy(zonePresenceTimer = state.value.zonePresenceTimer.copy(timer = cardTimer))
                }

                TimerType.ZoneShuffle -> _state.update {
                    it.copy(zoneShuffleTimer = state.value.zoneShuffleTimer.copy(timer = cardTimer))
                }

                TimerType.RunnerLocation -> _state.update {
                    it.copy(
                        runnerLocationUpdateTimer = state.value.runnerLocationUpdateTimer.copy(
                            timer = cardTimer
                        )
                    )
                }
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
                    myApp.cardsDao,
                    myApp.trueTime
                ) as T
            }
        }
    }
}
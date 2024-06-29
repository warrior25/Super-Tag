package com.huikka.supertag.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.ActiveRunnerZonesDao
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.CardsDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.helpers.GameStatus
import com.huikka.supertag.ui.events.MainEvent
import com.huikka.supertag.ui.state.MainState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val runnerDao: RunnerDao,
    private val activeRunnerZonesDao: ActiveRunnerZonesDao,
    private val cardsDao: CardsDao
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.OnHostGameClick -> hostGame()
            is MainEvent.OnJoinGameClick -> joinGame()
            is MainEvent.OnGameIdChange -> updateGameId(event.gameId)
            is MainEvent.OnInit -> initData()
            is MainEvent.OnNavigateAway -> resetIsInitialized()
        }
    }

    private fun resetIsInitialized() {
        _state.update {
            it.copy(
                isInitialized = false
            )
        }
    }

    private fun updateGameId(input: String) {
        _state.update {
            it.copy(
                gameId = input
            )
        }
    }

    private fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            getUsername()
            getGameStatus()
            _state.update {
                it.copy(
                    isInitialized = true
                )
            }
        }
    }

    private suspend fun getUsername() {
        val playerId = getPlayerId()
        if (playerId != null) {
            _state.update {
                it.copy(
                    username = playerDao.getPlayerById(playerId)!!.name ?: ""
                )
            }
        }
    }

    private suspend fun getPlayerId(): String? {
        authDao.awaitCurrentSession()
        return authDao.getUser()?.id
    }

    private fun joinGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val playerId = getPlayerId()
            try {
                playerDao.addToGame(
                    playerId!!, state.value.gameId
                )
                _state.update {
                    it.copy(
                        gameStatus = GameStatus.LOBBY
                    )
                }
                delay(1000)
                _state.update {
                    it.copy(
                        isInitialized = false
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to join game: $e")
                _state.update {
                    it.copy(
                        error = "Failed to join game"
                    )
                }
            }
        }
    }

    private fun hostGame() {
        viewModelScope.launch(Dispatchers.IO) {
            var newId: String
            while (true) {
                newId = List(6) { ('A'..'Z').random() }.joinToString("")
                if (!gameDao.checkGameExists(newId)) {
                    break
                }
            }

            try {
                gameDao.createGame(
                    Game(
                        id = newId, status = GameStatus.LOBBY, runnerId = getPlayerId()
                    )
                )
                playerDao.addToGame(getPlayerId()!!, newId, true)
                runnerDao.addGame(newId)
                activeRunnerZonesDao.addGame(newId)
                cardsDao.addGame(newId)
                _state.update {
                    it.copy(
                        gameStatus = GameStatus.LOBBY, gameId = newId
                    )
                }
                delay(1000)
                _state.update {
                    it.copy(
                        isInitialized = false
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to host game: $e")
                _state.update {
                    it.copy(
                        error = "Failed to host game"
                    )
                }
            }
        }
    }

    private suspend fun getGameStatus() {
        try {
            val playerId = getPlayerId()!!
            val player = playerDao.getPlayerById(playerId)!!
            if (player.gameId == null) {
                _state.update {
                    it.copy(
                        gameId = "", gameStatus = null
                    )
                }
                return
            }
            _state.update {
                it.copy(
                    gameId = player.gameId, gameStatus = gameDao.getGameStatus(player.gameId)
                )
            }
        } catch (e: Exception) {
            Log.e("LobbyViewModel", "Failed to get current game info: $e")
            _state.update {
                it.copy(
                    error = "Failed to get current game status"
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                val myApp = application as STApplication

                return MainViewModel(
                    myApp.authDao,
                    myApp.gameDao,
                    myApp.playerDao,
                    myApp.runnerDao,
                    myApp.activeRunnerZonesDao,
                    myApp.cardsDao,
                ) as T
            }
        }
    }
}
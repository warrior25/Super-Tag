package com.huikka.supertag.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.helpers.GameStatus
import com.huikka.supertag.ui.events.LobbyEvent
import com.huikka.supertag.ui.state.LobbyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyViewModel(
    private val authDao: AuthDao, private val gameDao: GameDao, private val playerDao: PlayerDao
) : ViewModel() {

    private val _state = MutableStateFlow(LobbyState())
    val state = _state.asStateFlow()

    fun onEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.OnLeaveGameClick -> leaveGame()
            is LobbyEvent.OnStartGameClick -> startGame()
            is LobbyEvent.OnRunnerChange -> setRunner(event.runnerId)
            is LobbyEvent.OnInit -> initData(event.gameId)
            is LobbyEvent.OnNavigateAway -> resetIsInitialized()
        }
    }

    private fun initData(gameId: String) {
        _state.update {
            it.copy(
                gameId = gameId
            )
        }
        getHostStatus()
        getPlayers()
        getGameData()
    }

    private fun resetIsInitialized() {
        _state.update {
            it.copy(
                isInitialized = false
            )
        }
    }

    private suspend fun getPlayerId(): String? {
        authDao.awaitCurrentSession()
        return authDao.getUser()?.id
    }

    private fun getHostStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val playerId = getPlayerId()!!
                val player = playerDao.getPlayerById(playerId)!!
                _state.update {
                    it.copy(
                        isHost = player.isHost!!
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
    }

    private fun startGame() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                gameDao.setGameStatus(state.value.gameId, GameStatus.PLAYING)
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Failed to get current game info: $e")
                _state.update {
                    it.copy(
                        error = "Failed to set new game status"
                    )
                }
            }
        }
    }

    private fun getPlayers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val flow = playerDao.getPlayersByGameIdFlow(state.value.gameId)
                flow.collect { players ->
                    Log.d("PLAYERS", players.toString())
                    _state.update {
                        it.copy(
                            players = players
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Failed to get players: $e")
                _state.update {
                    it.copy(
                        error = "Failed to get players"
                    )
                }
            }
        }
    }

    private fun getGameData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val flow = gameDao.getGameFlow(state.value.gameId)
                flow.collect { game ->
                    _state.update {
                        it.copy(
                            game = game, isInitialized = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Failed to get game data: $e")
                _state.update {
                    it.copy(
                        error = "Failed to get game data"
                    )
                }
            }
        }
    }

    private fun setRunner(runnerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                gameDao.setRunnerId(state.value.gameId, runnerId)
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Failed to set runner: $e")
                _state.update {
                    it.copy(
                        error = "Failed to set runner"
                    )
                }
            }
        }
    }

    private fun leaveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (state.value.isHost) {
                    gameDao.removeGame(state.value.gameId)
                } else {
                    playerDao.removeFromGame(getPlayerId()!!)
                }
                _state.update {
                    it.copy(
                        gameId = ""
                    )
                }
            } catch (e: Exception) {
                Log.e("LobbyViewModel", "Failed to leave game: $e")
                _state.update {
                    it.copy(
                        error = "Failed to leave game"
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

                return LobbyViewModel(
                    myApp.authDao, myApp.gameDao, myApp.playerDao
                ) as T
            }
        }
    }
}
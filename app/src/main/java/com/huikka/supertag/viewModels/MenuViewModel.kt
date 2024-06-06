package com.huikka.supertag.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.R
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.dto.Player
import com.huikka.supertag.data.helpers.GameStatuses
import com.huikka.supertag.data.helpers.PermissionErrors

class MenuViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val runnerDao: RunnerDao,
) : ViewModel() {

    var gameId by mutableStateOf("")
        private set

    var gameStatus by mutableStateOf<String?>(null)
        private set

    var username by mutableStateOf("")
        private set

    var players by mutableStateOf<List<Player>>(listOf())
        private set

    var isHost by mutableStateOf(false)
        private set

    var game by mutableStateOf<Game?>(null)
        private set

    var error by mutableStateOf("")
        private set

    var permissionErrorButtonTextId by mutableStateOf<Int?>(null)
        private set

    var permissionErrorInfoTextId by mutableStateOf<Int?>(null)
        private set

    var permissionError by mutableStateOf<PermissionErrors?>(null)

    fun updateGameId(input: String) {
        gameId = input
    }

    suspend fun updateUsername() {
        val playerId = getPlayerId()
        if (playerId != null) {
            username = playerDao.getPlayerById(playerId)!!.name ?: ""
        }
    }

    fun updatePermissionError(error: PermissionErrors?) {
        permissionError = error
        when (error) {
            PermissionErrors.NotRequested -> {
                permissionErrorInfoTextId = R.string.insufficient_permissions
                permissionErrorButtonTextId = R.string.fix_now
            }

            PermissionErrors.Denied -> {
                permissionErrorInfoTextId = R.string.permissions_denied
                permissionErrorButtonTextId = R.string.open_settings
            }

            else -> {
                permissionErrorInfoTextId = null
                permissionErrorButtonTextId = null
            }
        }
    }

    private suspend fun getPlayerId(): String? {
        authDao.awaitCurrentSession()
        return authDao.getUser()?.id
    }

    suspend fun joinGame() {
        val playerId = getPlayerId()
        try {
            playerDao.addToGame(
                playerId!!, gameId
            )
            gameStatus = GameStatuses.LOBBY
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to join game: $e")
            error = "Game not found"
        }
    }

    suspend fun logout() {
        try {
            authDao.logout()
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to logout: $e")
            error = "Failed to logout"
        }
    }

    suspend fun hostGame() {
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
                    id = newId, status = GameStatuses.LOBBY, runnerId = getPlayerId()
                )
            )
            playerDao.addToGame(getPlayerId()!!, newId, true)
            runnerDao.addGame(newId)
            gameStatus = GameStatuses.LOBBY
            gameId = newId
            isHost = true
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to host game: $e")
            error = "Failed to host game"
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getPlayerId() != null
    }

    suspend fun getGameStatus() {
        try {
            val playerId = getPlayerId()!!
            val player = playerDao.getPlayerById(playerId)!!
            if (player.gameId == null) {
                gameId = ""
                gameStatus = null
                return
            }
            gameId = player.gameId
            gameStatus = gameDao.getGameStatus(gameId)
            isHost = player.isHost!!
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to get current game info: $e")
            error = "Failed to get current game status"
        }
    }

    suspend fun setGameStatus(status: String) {
        try {
            val playerId = getPlayerId()!!
            val gameId = playerDao.getPlayerById(playerId)!!.gameId
            gameDao.setGameStatus(gameId!!, status)
            gameStatus = status
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to get current game info: $e")
            error = "Failed to set new game status"
        }
    }

    suspend fun getPlayers() {
        try {
            val flow = playerDao.getPlayersByGameIdFlow(gameId)
            flow.collect {
                players = it
                Log.d("PLAYERS", players.toString())
            }
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to get players: $e")
            error = "Failed to get players"
        }
    }

    suspend fun getGameData() {
        try {
            val flow = gameDao.getGameFlow(gameId)
            flow.collect {
                game = it
            }
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to get game data: $e")
            error = "Failed to get game data"
        }
    }

    suspend fun setRunner(runnerId: String) {
        try {
            gameDao.setRunnerId(gameId, runnerId)
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to set runner: $e")
            error = "Failed to set runner"
        }
    }

    suspend fun leaveGame() {
        try {
            playerDao.removeFromGame(getPlayerId()!!)
            gameStatus = null
            gameId = ""
        } catch (e: Exception) {
            Log.e("MenuViewModel", "Failed to leave game: $e")
            error = "Failed to leave game"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                val myApp = application as STApplication

                return MenuViewModel(
                    myApp.authDao, myApp.gameDao, myApp.playerDao, myApp.runnerDao
                ) as T
            }
        }
    }
}
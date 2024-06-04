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
import com.huikka.supertag.data.helpers.PermissionErrors

class MainMenuViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val runnerDao: RunnerDao,
) : ViewModel() {

    var gameId by mutableStateOf("")
        private set

    var username by mutableStateOf("")
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
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to join game: $e")
            error = "Game not found"
        }
    }

    suspend fun logout() {
        try {
            authDao.logout()
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to logout: $e")
            error = "Failed to logout"
        }
    }

    suspend fun hostGame() {
        var gameId: String
        while (true) {
            gameId = List(2) { ('A'..'Z').random() }.joinToString("")
            if (!gameDao.checkGameExists(gameId)) {
                break
            }
        }

        try {
            gameDao.createGame(
                Game(
                    gameId, "lobby"
                )
            )
            playerDao.addToGame(getPlayerId()!!, gameId, true)
            runnerDao.addGame(gameId)
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to host game: $e")
            error = "Failed to host game"
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getPlayerId() != null
    }

    suspend fun getCurrentGameStatus(): String? {
        try {
            val playerId = getPlayerId()!!
            val gameId = playerDao.getPlayerById(playerId)!!.gameId
            return gameDao.getGameStatus(gameId!!)
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to get current game info: $e")
            return null
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                val myApp = application as STApplication

                return MainMenuViewModel(
                    myApp.authDao, myApp.gameDao, myApp.playerDao, myApp.runnerDao
                ) as T
            }
        }
    }
}
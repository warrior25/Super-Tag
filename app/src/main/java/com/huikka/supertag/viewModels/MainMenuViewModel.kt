package com.huikka.supertag.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dto.Game

class MainMenuViewModel(
    private val authDao: AuthDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val runnerDao: RunnerDao,
) : ViewModel() {

    private suspend fun getPlayerId(): String? {
        authDao.awaitCurrentSession()
        return authDao.getUser()?.id
    }

    suspend fun joinGame(gameId: String): Error? {
        val playerId = getPlayerId()
        return try {
            playerDao.addToGame(
                playerId!!, gameId
            )
            null
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to join game: $e")
            Error(e)
        }
    }

    suspend fun logout(): Error? {
        return try {
            authDao.logout()
            null
        } catch (e: Exception) {
            Log.e("MainMenuViewModel", "Failed to logout: $e")
            Error(e)
        }
    }

    suspend fun hostGame(): Error? {
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
            return Error(e)
        }
        return null
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
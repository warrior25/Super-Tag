package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.CurrentGameDto
import com.huikka.supertag.data.dto.Game
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class GameDao(application: STApplication) {

    private val db: Postgrest = application.supabase.postgrest

    private suspend fun getGameById(id: String): Game {
        return db.from("games").select {
            filter {
                eq("id", id)
            }
        }.decodeSingle<Game>()
    }

    suspend fun checkGameExists(id: String): Boolean {
        return db.from("games").select {
            filter {
                eq("id", id)
            }
        }.countOrNull() != null
    }

    suspend fun removeChaser(id: String): Error? {
        try {
            db.from("players").update({ setToNull("gameId") }) {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun addChaser(playerId: String, gameId: String, isHost: Boolean = false): Error? {
        try {
            db.from("players").update({
                set("gameId", gameId)
                set("isHost", isHost)
            }) {
                filter {
                    eq("id", playerId)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun createGame(game: Game): Error? {
        try {
            val gameDto = Game(
                id = game.id,
                status = game.status,
                runner = game.runner,
                runnerMoney = game.runnerMoney,
                chaserMoney = game.chaserMoney,
                robberyInProgress = game.robberyInProgress,
                startTime = game.startTime,
                endTime = game.endTime,
                headStart = game.headStart,
                nextRunnerLocationUpdate = game.nextRunnerLocationUpdate,
                lastRunnerLocationUpdate = game.lastRunnerLocationUpdate
            )
            db.from("games").insert(gameDto)
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun removeGame(id: String): Error? {
        try {
            db.from("games").delete {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun getCurrentGameInfo(userId: String): CurrentGameDto {
        return db.from("players").select(columns = Columns.list("gameId", "isHost")) {
            filter {
                eq("id", userId)
            }
        }.decodeSingle<CurrentGameDto>()
    }
}
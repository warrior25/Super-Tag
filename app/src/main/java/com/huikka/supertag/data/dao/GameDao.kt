package com.huikka.supertag.data.dao

import android.util.Log
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.CurrentGame
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.dto.GameStatus
import com.huikka.supertag.data.dto.RunnerId
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow

class GameDao(application: STApplication) {

    private val db: Postgrest = application.supabase.postgrest

    suspend fun getGameStatus(id: String): String {
        return db.from("games").select(columns = Columns.list("status")) {
            filter {
                eq("id", id)
            }
        }.decodeSingle<GameStatus>().status
    }

    suspend fun checkGameExists(id: String): Boolean {
        return db.from("games").select {
            filter {
                eq("id", id)
            }
        }.countOrNull() != null
    }

    suspend fun setRunnerId(gameId: String, playerId: String): Error? {
        try {
            db.from("games").update({
                set("runner_id", playerId)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun getRunnerId(gameId: String): String? {
        return try {
            db.from("games").select(columns = Columns.list("runner_id")) {
                filter {
                    eq("id", gameId)
                }
            }.decodeSingle<RunnerId>().runnerId
        } catch (e: Exception) {
            Log.d("getRunnerId", e.toString())
            null
        }
    }

    suspend fun setGameStatus(gameId: String, status: String): Error? {
        try {
            db.from("games").update({
                set("status", status)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    @OptIn(SupabaseExperimental::class)
    fun getGameFlow(gameId: String): Flow<Game> {
        return db.from("games").selectSingleValueAsFlow(Game::id) {
            eq("id", gameId)
        }
    }

    suspend fun createGame(game: Game): Error? {
        try {
            val gameDto = Game(
                id = game.id,
                status = game.status,
                runnerId = game.runnerId,
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

    suspend fun getCurrentGameInfo(userId: String): CurrentGame {
        return db.from("players").select(columns = Columns.list("game_id", "is_host")) {
            filter {
                eq("id", userId)
            }
        }.decodeSingle<CurrentGame>()
    }

    suspend fun getNextLocationUpdate(gameId: String): String? {
        return db.from("games").select(columns = Columns.list("next_runner_location_update")) {
            filter {
                eq("id", gameId)
            }
        }.decodeSingle<Game>().nextRunnerLocationUpdate
    }

    suspend fun setNextLocationUpdate(gameId: String, nextLocationUpdate: String): Error? {
        try {
            db.from("games").update({
                set("next_runner_location_update", nextLocationUpdate)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
        } catch (e: Exception) {
            return Error(e)
        }
        return null
    }

    suspend fun setHeadStart(gameId: String, headStart: Int): Error? {
        return try {
            db.from("games").update({
                set("head_start", headStart)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
            null
        } catch (e: Exception) {
            Error(e)
        }
    }

    suspend fun getHeadStart(gameId: String): Int? {
        return db.from("games").select(columns = Columns.list("head_start")) {
            filter {
                eq("id", gameId)
            }
        }.decodeSingle<Game>().headStart
    }
}
package com.huikka.supertag.data.dao

import android.util.Log
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.CurrentGame
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.dto.GameStatus
import com.huikka.supertag.data.helpers.Sides
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
            }.decodeSingle<Game>().runnerId
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

    suspend fun createGame(game: Game) {
        db.from("games").insert(game)
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

    suspend fun getHeadStart(gameId: String): Int? {
        return db.from("games").select(columns = Columns.list("head_start")) {
            filter {
                eq("id", gameId)
            }
        }.decodeSingle<Game>().headStart
    }

    suspend fun getInitialTrackingInterval(gameId: String): Int? {
        return db.from("games").select(columns = Columns.list("initial_tracking_interval")) {
            filter {
                eq("id", gameId)
            }
        }.decodeSingle<Game>().initialTrackingInterval
    }

    suspend fun addMoney(gameId: String, side: Sides, amount: Int): Error? {
        return try {
            val newMoney = getMoney(gameId, side)!! + amount
            var column = "chaser_money"
            if (side == Sides.Runner) {
                column = "runner_money"
            }
            db.from("games").update({
                set(column, newMoney)
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

    suspend fun reduceMoney(gameId: String, side: Sides, amount: Int): Error? {
        return try {
            val newMoney = getMoney(gameId, side)!! - amount
            var column = "chaser_money"
            if (side == Sides.Runner) {
                column = "runner_money"
            }
            db.from("games").update({
                set(column, newMoney)
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

    private suspend fun getMoney(gameId: String, side: Sides): Int? {
        val game = db.from("games").select(columns = Columns.list("chaser_money", "runner_money")) {
            filter {
                eq("id", gameId)
            }
        }.decodeSingle<Game>()
        if (side == Sides.Runner) {
            return game.runnerMoney
        }
        return game.chaserMoney
    }

    suspend fun changeSettings(
        gameId: String, chaserMoney: Int, runnerMoney: Int, headStart: Int
    ): Error? {
        return try {
            db.from("games").update({
                set("chaser_money", chaserMoney)
                set("runner_money", runnerMoney)
                set("head_start", headStart)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
            null
        } catch (e: Exception) {
            Log.e("changeSettings", e.toString())
            Error(e)
        }
    }

    suspend fun getActiveRunnerZones(gameId: String): List<Int>? {
        return try {
            db.from("games").select(columns = Columns.list("active_runner_zones")) {
                filter {
                    eq("id", gameId)
                }
            }.decodeSingle<Game>().activeRunnerZones
        } catch (e: Exception) {
            Log.d("getActiveRunnerZones", e.toString())
            null
        }
    }

    suspend fun setActiveRunnerZones(gameId: String, zones: List<Int>) {
        try {
            db.from("games").update({
                set("active_runner_zones", zones)
            }) {
                filter {
                    eq("id", gameId)
                }
            }
        } catch (e: Exception) {
            Log.d("getActiveRunnerZones", e.toString())
        }
    }
}
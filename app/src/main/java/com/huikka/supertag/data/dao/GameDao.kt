package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.CurrentGame
import com.huikka.supertag.data.dto.Game
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow

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
            }.decodeSingleOrNull<String>()
        } catch (e: Exception) {
            null
        }
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
}
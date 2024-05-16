package com.huikka.supertag.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huikka.supertag.data.room.CurrentGame

@Dao
interface CurrentGameDao {
    @Query("SELECT * FROM current_game LIMIT 1")
    suspend fun getGameDetails(): CurrentGame?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameDetails(game: CurrentGame)

    @Query("DELETE FROM current_game")
    suspend fun deleteGameDetails()
}
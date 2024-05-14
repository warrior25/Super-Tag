package com.huikka.supertag.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_game")
data class CurrentGame(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "is_host") val isHost: Boolean
)

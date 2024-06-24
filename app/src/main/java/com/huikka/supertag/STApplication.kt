package com.huikka.supertag

import android.app.Application
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dao.RunnerDao
import com.huikka.supertag.data.dao.ZoneDao

class STApplication : Application() {
    //private val database by lazy { AppDatabase.getDatabase(this) }
    //val currentGameDao by lazy { database.currentGameDao() }
    val supabase by lazy { SupabaseModule.getSupabaseClient() }
    val authDao by lazy { AuthDao(this) }
    val playerDao by lazy { PlayerDao(this) }
    val gameDao by lazy { GameDao(this) }
    val runnerDao by lazy { RunnerDao(this) }
    val zoneDao by lazy { ZoneDao(this) }
}
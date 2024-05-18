package com.huikka.supertag

import android.app.Application

class STApplication : Application() {
    //private val database by lazy { AppDatabase.getDatabase(this) }
    //val currentGameDao by lazy { database.currentGameDao() }
    val supabase by lazy { SupabaseModule.getSupabaseClient() }
}
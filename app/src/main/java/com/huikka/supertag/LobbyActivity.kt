package com.huikka.supertag

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.huikka.supertag.data.Dao
import com.huikka.supertag.data.LoginDataSource
import com.huikka.supertag.data.LoginRepository

class LobbyActivity : AppCompatActivity() {

    private lateinit var loginRepository: LoginRepository
    private var db = Dao()
    private lateinit var gameId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameId = intent.getStringExtra("GAME_ID")!!

        // Setup firebase user authentication
        val loginDataSource = LoginDataSource()
        loginRepository = LoginRepository(loginDataSource)
    }

    private suspend fun leaveGame() {
        db.removeChaser(loginRepository.user?.uid!!, gameId)
    }
}
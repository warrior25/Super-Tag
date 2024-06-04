package com.huikka.supertag

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var gameDao: GameDao
    private lateinit var authDao: AuthDao

    private lateinit var headStartSlider: CustomSlider
    private lateinit var runnerCoinsSlider: CustomSlider
    private lateinit var chaserCoinsSlider: CustomSlider
    private lateinit var saveButton: Button

    private lateinit var playerId: String
    private lateinit var gameId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        headStartSlider = findViewById(R.id.headStart)
        runnerCoinsSlider = findViewById(R.id.runnerCoins)
        chaserCoinsSlider = findViewById(R.id.chaserCoins)
        saveButton = findViewById(R.id.saveButton)

        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as STApplication
            gameDao = GameDao(app)
            authDao = AuthDao(app)
            playerId = authDao.getUser()!!.id
            gameId = gameDao.getCurrentGameInfo(playerId).gameId!!
        }

        saveButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                saveSettings()
                finish()
            }
        }

    }

    suspend fun saveSettings() {
        val headStart = headStartSlider.getValue()
        val runnerCoins = runnerCoinsSlider.getValue()
        val chaserCoins = chaserCoinsSlider.getValue()
        gameDao.changeSettings(
            gameId, headStart.toInt(), runnerCoins.toInt(), chaserCoins.toInt()
        )
        Log.d("SETTINGS", "Updated game settings")
    }
}
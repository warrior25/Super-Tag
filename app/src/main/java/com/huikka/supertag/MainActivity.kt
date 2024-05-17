package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.data.dao.GameDao
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.data.room.CurrentGame
import com.huikka.supertag.data.room.dao.CurrentGameDao
import com.huikka.supertag.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var gameDao: GameDao
    private lateinit var authDao: AuthDao

    private lateinit var currentGameDao: CurrentGameDao

    // UI elements
    private lateinit var joinGameButton: Button
    private lateinit var hostGameButton: FloatingActionButton
    private lateinit var gameIdEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as STApplication
        authDao = AuthDao(app)
        gameDao = GameDao(app)
        currentGameDao = app.currentGameDao

        // Setup button actions
        joinGameButton = findViewById(R.id.joinGameButton)
        gameIdEditText = findViewById(R.id.gameIdEditText)
        joinGameButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                joinGame(gameIdEditText.text.toString())
            }
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                authDao.logout()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        hostGameButton = findViewById(R.id.hostGameButton)
        hostGameButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                hostGame()
            }
        }

        val loading = findViewById<ProgressBar>(R.id.loading)

        CoroutineScope(Dispatchers.Main).launch {
            val session = authDao.awaitCurrentSession()
            if (session == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val currentGame = gameDao.getCurrentGameInfo(authDao.getUser()!!.id)
                Log.d("CURRENT_GAME", currentGame.toString())
                if (currentGame.gameId != null) {
                    startLobbyActivity(currentGame.gameId, currentGame.isHost)
                }
            }
            loading.visibility = View.GONE
            hostGameButton.visibility = View.VISIBLE
        }

        val requestBackgroundLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


        val requestFineLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            } else {
                requestBackgroundLocation.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestFineLocation.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocation.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    private suspend fun hostGame() {
        var gameId: String
        while (true) {
            gameId = List(6) { ('A'..'Z').random() }.joinToString("")
            if (!gameDao.checkGameExists(gameId)) {
                break
            }
        }

        var err = gameDao.createGame(
            Game(
                gameId, "lobby"
            )
        )

        if (err != null) {
            Log.e("HOST", "Failed to host game: $err")
            return
        }

        err = gameDao.addPlayer(authDao.getUser()!!.id, gameId, true)

        if (err != null) {
            Log.e("HOST", "Failed to host game: $err")
            return
        }

        startLobbyActivity(gameId, true)
    }

    private suspend fun joinGame(gameId: String) {
        val err = gameDao.addPlayer(
            authDao.getUser()!!.id, gameId
        )
        if (err != null) {
            Toast.makeText(
                applicationContext, "Game $gameId does not exist", Toast.LENGTH_LONG
            ).show()
            return
        }
        Toast.makeText(
            applicationContext, "Joined game $gameId", Toast.LENGTH_LONG
        ).show()

        startLobbyActivity(gameId)

    }

    private fun startLobbyActivity(gameId: String, host: Boolean = false) {
        val intent = Intent(this, LobbyActivity::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            currentGameDao.deleteGameDetails()
            currentGameDao.insertGameDetails(CurrentGame(gameId, host))
            startActivity(intent)
            gameIdEditText.text.clear()
            gameIdEditText.clearFocus()
        }
    }
}
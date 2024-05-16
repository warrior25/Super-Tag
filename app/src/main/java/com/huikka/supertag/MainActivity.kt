package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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
import com.huikka.supertag.data.dao.PlayerDao
import com.huikka.supertag.data.dto.Game
import com.huikka.supertag.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var gameDao: GameDao
    private lateinit var authDao: AuthDao
    private lateinit var playerDao: PlayerDao

    private lateinit var playerId: String

    // UI elements
    private lateinit var joinGameButton: Button
    private lateinit var hostGameButton: FloatingActionButton
    private lateinit var gameIdEditText: EditText
    private lateinit var permissionsError: TextView
    private lateinit var fixPermissionsButton: Button

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
        playerDao = PlayerDao(app)

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

            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)

            //CoroutineScope(Dispatchers.Main).launch {
            //    hostGame()
            //}
        }

        val loading = findViewById<ProgressBar>(R.id.loading)

        CoroutineScope(Dispatchers.Main).launch {
            val session = authDao.awaitCurrentSession()
            if (session == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                playerId = authDao.getUser()!!.id
                val currentGame = gameDao.getCurrentGameInfo(playerId)
                if (currentGame.gameId != null) {
                    startLobbyActivity()
                }
            }
            loading.visibility = View.GONE
        }

        permissionsError = findViewById(R.id.permissionsInfoText)
        fixPermissionsButton = findViewById(R.id.fixPermissionsButton)

        var locationPermissionFailed = false

        val requestBackgroundLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                locationPermissionFailed = true
                locationPermissionDenied()
            } else {
                showPermissionsError(false)
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
                locationPermissionFailed = true
                locationPermissionDenied()
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                requestBackgroundLocation.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        fixPermissionsButton.setOnClickListener {
            if (locationPermissionFailed) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                requestFineLocation.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showPermissionsError(false)
            } else {
                showPermissionsError(true)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showPermissionsError(false)
            } else {
                showPermissionsError(true)
            }
        }
    }

    private fun showPermissionsError(show: Boolean) {
        if (show) {
            hostGameButton.visibility = View.GONE
            joinGameButton.visibility = View.GONE
            gameIdEditText.visibility = View.GONE
            permissionsError.visibility = View.VISIBLE
            fixPermissionsButton.visibility = View.VISIBLE

        } else {
            hostGameButton.visibility = View.VISIBLE
            joinGameButton.visibility = View.VISIBLE
            gameIdEditText.visibility = View.VISIBLE
            permissionsError.visibility = View.GONE
            fixPermissionsButton.visibility = View.GONE
        }
    }

    private fun locationPermissionDenied() {
        fixPermissionsButton.text = getText(R.string.open_settings)
        permissionsError.text = getText(R.string.permissions_denied)
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

        err = playerDao.addToGame(playerId, gameId, true)

        if (err != null) {
            Log.e("HOST", "Failed to host game: $err")
            return
        }

        startLobbyActivity()
    }

    private suspend fun joinGame(gameId: String) {
        val err = playerDao.addToGame(
            playerId, gameId
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

        startLobbyActivity()
    }

    private fun startLobbyActivity() {
        val intent = Intent(this, LobbyActivity::class.java)
        CoroutineScope(Dispatchers.Main).launch {
            startActivity(intent)
            gameIdEditText.text.clear()
            gameIdEditText.clearFocus()
        }
    }
}
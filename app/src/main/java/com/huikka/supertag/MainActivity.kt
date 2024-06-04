package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huikka.supertag.data.helpers.GameStatuses
import com.huikka.supertag.ui.login.LoginActivity
import com.huikka.supertag.viewModels.MainMenuViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var joinGameButton: Button
    private lateinit var hostGameButton: FloatingActionButton
    private lateinit var gameIdEditText: EditText
    private lateinit var permissionsError: TextView
    private lateinit var fixPermissionsButton: Button

    private val mmvm: MainMenuViewModel by viewModels { MainMenuViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup button actions
        joinGameButton = findViewById(R.id.joinGameButton)
        gameIdEditText = findViewById(R.id.gameIdEditText)
        joinGameButton.setOnClickListener {
            lifecycleScope.launch {
                val err = mmvm.joinGame(gameIdEditText.text.toString())
                if (err != null) {
                    Toast.makeText(
                        applicationContext,
                        "Game ${gameIdEditText.text} does not exist",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                startLobbyActivity()
            }
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                val err = mmvm.logout()
                if (err != null) {
                    Toast.makeText(
                        applicationContext, "Failed to logout", Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        hostGameButton = findViewById(R.id.hostGameButton)
        hostGameButton.setOnClickListener {
            lifecycleScope.launch {
                val err = mmvm.hostGame()
                if (err != null) {
                    Toast.makeText(
                        applicationContext, "Failed to host game", Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                startLobbyActivity()
            }
        }

        val loading = findViewById<ProgressBar>(R.id.loading)

        lifecycleScope.launch {
            if (!mmvm.isLoggedIn()) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return@launch
            }

            val currentGameStatus = mmvm.getCurrentGameStatus()
            when (currentGameStatus) {
                GameStatuses.LOBBY -> {
                    startLobbyActivity()
                }

                GameStatuses.PLAYING -> {
                    startGameActivity()
                }
            }
            loading.visibility = View.GONE
        }

        permissionsError = findViewById(R.id.permissionsInfoText)
        fixPermissionsButton = findViewById(R.id.fixPermissionsButton)
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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

    private fun requestPermissions() {
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
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ), 0
            )
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

    private fun startLobbyActivity() {
        val intent = Intent(this, LobbyActivity::class.java)
        startActivity(intent)
    }

    private fun startGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
}
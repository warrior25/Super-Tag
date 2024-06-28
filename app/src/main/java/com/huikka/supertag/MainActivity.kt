package com.huikka.supertag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import com.huikka.supertag.data.helpers.PermissionErrors
import com.huikka.supertag.ui.Navigation
import com.huikka.supertag.viewModels.GameViewModel
import com.huikka.supertag.viewModels.LobbySettingsViewModel
import com.huikka.supertag.viewModels.LobbyViewModel
import com.huikka.supertag.viewModels.LoginViewModel
import com.huikka.supertag.viewModels.MainViewModel
import com.huikka.supertag.viewModels.PermissionErrorViewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val lobbyViewModel: LobbyViewModel by viewModels { LobbyViewModel.Factory }
    private val loginViewModel: LoginViewModel by viewModels { LoginViewModel.Factory }
    private val lobbySettingsViewModel: LobbySettingsViewModel by viewModels { LobbySettingsViewModel.Factory }
    private val gameViewModel: GameViewModel by viewModels { GameViewModel.Factory }
    private val permissionErrorViewModel: PermissionErrorViewModel by viewModels()

    private lateinit var requestBackgroundLocation: ActivityResultLauncher<String>
    private lateinit var requestFineLocation: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by permissionErrorViewModel.state.collectAsState()

            LaunchedEffect(state.permissionsRequested) {
                if (state.permissionsRequested) {
                    requestPermissions()
                    permissionErrorViewModel.resetPermissionsRequested()
                }
            }

            Navigation(
                mainViewModel = mainViewModel,
                loginViewModel = loginViewModel,
                lobbyViewModel = lobbyViewModel,
                lobbySettingsViewModel = lobbySettingsViewModel,
                gameViewModel = gameViewModel,
                permissionErrorViewModel = permissionErrorViewModel
            )
        }

        requestBackgroundLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                permissionErrorViewModel.updatePermissionError(PermissionErrors.Denied)
                Log.d("PERMISSIONS", "Denied background")
            } else {
                permissionErrorViewModel.updatePermissionError(null)
                Log.d("PERMISSIONS", "Granted background")
            }
        }


        requestFineLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                permissionErrorViewModel.updatePermissionError(PermissionErrors.Denied)
                Log.d("PERMISSIONS", "Denied foreground")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocation.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                Log.d("PERMISSIONS", "Granted foreground")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ), 0
            )
        }

        // Check location permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (permissionErrorViewModel.state.value.permissionError == null) {
                    permissionErrorViewModel.updatePermissionError(PermissionErrors.NotRequested)
                }
            } else {
                permissionErrorViewModel.updatePermissionError(null)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (permissionErrorViewModel.state.value.permissionError == null) {
                    permissionErrorViewModel.updatePermissionError(PermissionErrors.NotRequested)
                }
            } else {
                permissionErrorViewModel.updatePermissionError(null)
            }
        }
    }

    private fun requestPermissions() {
        if (permissionErrorViewModel.state.value.permissionError == PermissionErrors.NotRequested) {
            requestFineLocation.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else if (permissionErrorViewModel.state.value.permissionError == PermissionErrors.Denied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }
}
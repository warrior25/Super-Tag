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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.huikka.supertag.data.helpers.GameStatuses
import com.huikka.supertag.data.helpers.PermissionErrors
import com.huikka.supertag.ui.components.FloatingActionButtonWithText
import com.huikka.supertag.ui.components.LobbyActionButtons
import com.huikka.supertag.ui.components.PlayerListItem
import com.huikka.supertag.ui.login.LoginActivity
import com.huikka.supertag.viewModels.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MenuViewModel by viewModels { MenuViewModel.Factory }

    private lateinit var requestBackgroundLocation: ActivityResultLauncher<String>
    private lateinit var requestFineLocation: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (!viewModel.isLoggedIn()) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return@launch
            }

            viewModel.updateUsername()
            viewModel.getGameStatus()
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
                viewModel.updatePermissionError(PermissionErrors.Denied)
                Log.d("PERMISSIONS", "Denied background")
            } else {
                viewModel.updatePermissionError(null)
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
                viewModel.updatePermissionError(PermissionErrors.Denied)
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
                if (viewModel.permissionError == null) {
                    viewModel.updatePermissionError(PermissionErrors.NotRequested)
                }
            } else {
                viewModel.updatePermissionError(null)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (viewModel.permissionError == null) {
                    viewModel.updatePermissionError(PermissionErrors.NotRequested)
                }
            } else {
                viewModel.updatePermissionError(null)
            }
        }
    }

    @Composable
    fun MainScreen() {
        LaunchedEffect(viewModel.gameStatus) {
            if (viewModel.gameStatus == GameStatuses.LOBBY) {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getGameData()
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getPlayers()
                }
            }
        }

        when (viewModel.gameStatus) {
            GameStatuses.LOBBY -> {
                Lobby()
            }

            GameStatuses.PLAYING -> {
                startGameActivity()
            }

            null -> {
                MainMenu()
            }
        }
    }

    @Composable
    fun MainMenu() {
        if (viewModel.username == "") {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(id = R.string.loading), fontSize = 22.sp)
            }
            return
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Text(text = viewModel.username, fontSize = 22.sp)
            FilledTonalButton(onClick = {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.logout()
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }) {
                Text(stringResource(id = R.string.logout))
            }
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (viewModel.permissionErrorInfoTextId != null) {
                Column(
                    modifier = Modifier
                        .weight(1f, true)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = viewModel.permissionErrorInfoTextId!!),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        requestPermissions()
                    }) {
                        Text(stringResource(id = viewModel.permissionErrorButtonTextId!!))
                    }
                }
                return
            }
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = viewModel.error, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = viewModel.gameId,
                    onValueChange = { gameId ->
                        if (gameId.length <= 6 && (gameId.matches(Regex("^[A-Za-z]+\$")) or gameId.isEmpty())) {
                            viewModel.updateGameId(gameId.uppercase())
                        }
                    },
                    label = { Text(stringResource(id = R.string.game_id)) },

                    )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { lifecycleScope.launch(Dispatchers.IO) { viewModel.joinGame() } },
                    enabled = viewModel.gameId.length == 6
                ) {
                    Text(stringResource(id = R.string.join_game))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                FloatingActionButtonWithText(icon = { Icon(Icons.Filled.Add, "Host game") },
                    text = { Text(text = stringResource(id = R.string.host_game)) }) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.hostGame()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Lobby() {
        Scaffold(
            topBar = {
                TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ), title = {
                    Text(viewModel.gameId)
                }, actions = {
                    LobbyActionButtons({ lifecycleScope.launch(Dispatchers.IO) { viewModel.leaveGame() } },
                        { startSettingsActivity() })
                })
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, true)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(viewModel.players) { player ->
                            PlayerListItem(
                                player = player, isRunner = player.id == viewModel.game!!.runnerId
                            ) {
                                if (viewModel.isHost) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.setRunner(player.id!!)
                                    }
                                }
                            }
                        }
                    }
                }

                if (viewModel.isHost) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(onClick = {
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.setGameStatus(GameStatuses.PLAYING)
                                startGameActivity()
                            }
                        }) {
                            Text(stringResource(id = R.string.start_game))
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {

        if (viewModel.permissionError == PermissionErrors.Denied) {
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

    private fun startGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
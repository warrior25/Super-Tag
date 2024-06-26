package com.huikka.supertag.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.huikka.supertag.LocationUpdateService
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.data.helpers.ServiceActions
import com.huikka.supertag.data.helpers.ServiceStatus
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.ui.MainScreenRoute
import com.huikka.supertag.ui.components.ConfirmationDialog
import com.huikka.supertag.ui.components.Loading
import com.huikka.supertag.ui.components.Timer
import com.huikka.supertag.ui.components.map.ATM
import com.huikka.supertag.ui.components.map.Attraction
import com.huikka.supertag.ui.components.map.Player
import com.huikka.supertag.ui.components.map.Store
import com.huikka.supertag.ui.components.map.Zone
import com.huikka.supertag.ui.events.GameEvent
import com.huikka.supertag.ui.state.GameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController, state: GameState, onEvent: (GameEvent) -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    var showLeaveGameDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        if (!ServiceStatus.isServiceRunning(context)) {
            val intent = Intent(context, LocationUpdateService::class.java)
            intent.setAction(ServiceActions.START_SERVICE)
            startForegroundService(context, intent)
            ServiceStatus.setServiceRunning(context, true)
        }
        onEvent(GameEvent.OnInit)
    }

    if (!state.isInitialized) {
        Loading()
        return
    }

    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = {
            if (state.isRunner) {
                Text(text = stringResource(id = R.string.runner))
            } else {
                Text(text = stringResource(id = R.string.chaser))
            }
        }, actions = {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(text = {
                    Text(stringResource(id = R.string.how_to_play))
                }, onClick = { /* TODO */ }, enabled = false
                )
                DropdownMenuItem(text = {
                    Text(stringResource(id = R.string.leave_game))
                }, onClick = { showLeaveGameDialog = true })
            }
        })
    }, bottomBar = {
        BottomAppBar(actions = {
            if (state.isRunner) {
                Timer(
                    startTime = state.zoneUpdateTimer.time,
                    totalTime = Config.RUNNER_ZONE_SHUFFLE_TIME,
                    isTimerRunning = state.zoneUpdateTimer.isRunning,
                    handleColor = MaterialTheme.colorScheme.primary,
                    inactiveBarColor = Color.Gray,
                    activeBarColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(75.dp),
                    title = stringResource(id = R.string.zone)
                )
            } else {
                Timer(
                    startTime = state.runnerLocationUpdateTimer.time,
                    totalTime = state.runnerLocationUpdateTimer.time,
                    isTimerRunning = state.runnerLocationUpdateTimer.isRunning,
                    handleColor = MaterialTheme.colorScheme.primary,
                    inactiveBarColor = Color.Gray,
                    activeBarColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(75.dp),
                    title = stringResource(id = R.string.runner)
                )
            }
        })
    }) { padding ->
        if (showLeaveGameDialog) {
            val text = if (state.isRunner) {
                stringResource(id = R.string.leave_game_confirm_text_runner)
            } else {
                stringResource(id = R.string.leave_game_confirm_text_chaser)
            }
            ConfirmationDialog(text = text,
                title = stringResource(id = R.string.leave_game),
                icon = ImageVector.vectorResource(id = R.drawable.runner),
                confirmText = stringResource(id = R.string.confirm),
                dismissText = stringResource(id = R.string.cancel),
                onConfirm = {
                    val intent = Intent(context, LocationUpdateService::class.java)
                    intent.setAction(ServiceActions.STOP_SERVICE)
                    startForegroundService(context, intent)
                    ServiceStatus.setServiceRunning(context, false)
                    onEvent(GameEvent.OnLeaveGame)
                    navController.navigate(MainScreenRoute)
                },
                onDismiss = { showLeaveGameDialog = false })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val hervantaCampus = LatLng(61.4498, 23.8595)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(hervantaCampus, 15f)
            }

            val properties by remember {
                mutableStateOf(MapProperties(isMyLocationEnabled = true))
            }

            Box(contentAlignment = Alignment.Center) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = properties
                ) {
                    Zone(zone = state.playingArea)

                    if (state.isRunner) {
                        for (zone in state.activeRunnerZones) {
                            Attraction(zone = zone)
                        }
                    } else {
                        for (zone in state.chaserZones) {
                            if (zone.type == ZoneTypes.ATM) {
                                ATM(zone = zone)
                            } else if (zone.type == ZoneTypes.STORE) {
                                Store(zone = zone)
                            }
                        }
                        if (state.runner?.latitude != null) {
                            Player(
                                name = state.runnerName,
                                role = stringResource(id = R.string.runner),
                                latitude = state.runner.latitude,
                                longitude = state.runner.longitude!!,
                                accuracy = state.runner.locationAccuracy!!.toDouble(),
                                icon = BitmapDescriptorFactory.fromResource(R.drawable.agent)
                            )
                        }
                    }
                }
            }
        }
    }
}
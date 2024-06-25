package com.huikka.supertag.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.huikka.supertag.LocationUpdateService
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.data.helpers.ServiceStatus
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.ui.components.Loading
import com.huikka.supertag.ui.components.Timer
import com.huikka.supertag.ui.components.map.ATM
import com.huikka.supertag.ui.components.map.Attraction
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
    LaunchedEffect(true) {
        if (!ServiceStatus.isServiceRunning(context)) {
            val intent = Intent(context, LocationUpdateService::class.java)
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
            Text("Game")
        }, actions = {

        })
    }, bottomBar = {
        BottomAppBar(actions = {
            Timer(
                startTime = state.zoneUpdateTimer.time,
                totalTime = Config.RUNNER_ZONE_SHUFFLE_TIME,
                isTimerRunning = state.zoneUpdateTimer.isRunning,
                handleColor = MaterialTheme.colorScheme.primary,
                inactiveBarColor = Color.Gray,
                activeBarColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(75.dp),
                title = "Zone"
            )
        })
    }) { padding ->
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
                    Zone(zone = state.playingArea!!)

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
                    }

                }
            }
        }
    }
}
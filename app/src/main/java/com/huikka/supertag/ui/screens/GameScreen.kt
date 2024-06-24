package com.huikka.supertag.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.huikka.supertag.LocationUpdateService
import com.huikka.supertag.data.helpers.ZoneTypes
import com.huikka.supertag.ui.components.Loading
import com.huikka.supertag.ui.components.map.ATM
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
        val intent = Intent(context, LocationUpdateService::class.java)
        startForegroundService(context, intent)
        onEvent(GameEvent.OnInit)
    }
    if (!state.isInitialized) {
        Loading()
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text("Game")
            }, actions = {

            })
        },
    ) { padding ->
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

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties
            ) {
                Zone(zone = state.playingArea!!)

                if (true /* TODO: is chaser */) {
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
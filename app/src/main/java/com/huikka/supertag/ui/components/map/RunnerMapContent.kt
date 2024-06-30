package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.huikka.supertag.R
import com.huikka.supertag.ui.state.CardState
import com.huikka.supertag.ui.state.GameState

@Composable
fun RunnerMapContent(state: GameState, cardStates: List<CardState>) {
    for (zone in state.activeRunnerZones) {
        Attraction(zone = zone)
    }
    if (cardStates[2].timerState.isRunning) {
        for (player in state.players) {
            if (player.id != state.userId) {
                Player(
                    name = player.name,
                    role = stringResource(id = R.string.chaser),
                    latitude = player.latitude!!,
                    longitude = player.longitude!!,
                    accuracy = player.locationAccuracy!!.toDouble(),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_player),
                    color = Color.Magenta
                )
            }
        }
    }
}
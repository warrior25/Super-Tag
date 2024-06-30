package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.ZoneType
import com.huikka.supertag.ui.state.CardState
import com.huikka.supertag.ui.state.GameState

@Composable
fun ChaserMapContent(state: GameState, cardStates: List<CardState>) {
    for (zone in state.chaserZones) {
        if (zone.type == ZoneType.ATM) {
            ATM(zone = zone)
        } else if (zone.type == ZoneType.STORE) {
            Store(zone = zone)
        }
    }
    if (state.runner?.latitude != null && cardStates[1].activeUntil == null) {
        Player(
            name = state.runnerName,
            role = stringResource(id = R.string.runner),
            latitude = state.runner.latitude,
            longitude = state.runner.longitude!!,
            accuracy = state.runner.locationAccuracy!!.toDouble(),
            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_runner)
        )
    }
    for (player in state.players) {
        if (player.id !in listOf(
                state.runnerId, state.userId
            )
        ) {
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
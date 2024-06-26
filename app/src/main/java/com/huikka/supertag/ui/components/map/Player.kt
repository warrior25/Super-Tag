package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun Player(
    name: String,
    role: String,
    latitude: Double,
    longitude: Double,
    icon: BitmapDescriptor,
    accuracy: Double = 0.0,
    color: Color = Color.Black,
) {
    val markerState = rememberMarkerState(position = LatLng(latitude, longitude))
    if (accuracy > 0) {
        Circle(
            center = LatLng(latitude, longitude),
            radius = accuracy,
            fillColor = color.copy(alpha = 0.2f),
            strokeColor = color.copy(alpha = 0.6f)
        )
    }
    Marker(icon = icon, state = markerState, title = name, snippet = role, onClick = {
        markerState.showInfoWindow()
        true
    })
}
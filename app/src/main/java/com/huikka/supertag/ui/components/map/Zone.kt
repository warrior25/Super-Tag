package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.huikka.supertag.data.dto.Zone

@Composable
fun Zone(
    zone: Zone,
    color: Color? = null,
    title: String? = null,
    snippet: String? = null,
    icon: BitmapDescriptor? = null
) {
    val markerState = rememberMarkerState(position = LatLng(zone.latitude!!, zone.longitude!!))

    LaunchedEffect(zone) {
        markerState.position = LatLng(zone.latitude, zone.longitude)
    }
    Circle(
        center = LatLng(zone.latitude, zone.longitude),
        radius = zone.radius!!.toDouble(),
        fillColor = color?.copy(alpha = 0.2f) ?: Color.Transparent,
        strokeColor = color?.copy(alpha = 0.6f) ?: Color.Black
    )
    if (icon != null) {
        Marker(icon = icon, state = markerState, title = title, snippet = snippet, onClick = {
            markerState.showInfoWindow()
            true
        })
    }
}
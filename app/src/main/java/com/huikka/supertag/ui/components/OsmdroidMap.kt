package com.huikka.supertag.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.OverlayManagerState
import com.utsman.osmandcompose.rememberCameraState
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

@Composable
fun OsmdroidMap(overlayManagerState: OverlayManagerState) {
    var mapProperties by remember {
        mutableStateOf(DefaultMapProperties)
    }

    SideEffect {
        mapProperties = mapProperties.copy(isTilesScaledToDpi = true)
            .copy(tileSources = TileSourceFactory.MAPNIK).copy(isMultiTouchControls = true)
    }

    val cameraState = rememberCameraState {
        geoPoint = GeoPoint(61.4498, 23.8595)
        zoom = 15.0
    }
    OpenStreetMap(
        modifier = Modifier.fillMaxSize(),
        cameraState = cameraState,
        properties = mapProperties,
        overlayManagerState = overlayManagerState
    )
}
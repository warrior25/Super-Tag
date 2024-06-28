package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.huikka.supertag.R
import com.huikka.supertag.data.dto.Zone

@Composable
fun Attraction(zone: Zone) {
    Zone(
        zone = zone,
        title = zone.name,
        snippet = stringResource(id = R.string.attraction_snippet),
        icon = BitmapDescriptorFactory.fromResource(R.drawable.agent),
        color = Color.Blue
    )
}
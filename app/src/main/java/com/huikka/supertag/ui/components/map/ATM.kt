package com.huikka.supertag.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.huikka.supertag.R
import com.huikka.supertag.data.dto.Zone

@Composable
fun ATM(zone: Zone) {
    Zone(
        zone = zone,
        color = Color.Green,
        title = stringResource(id = R.string.atm),
        snippet = stringResource(
            id = R.string.atm_snippet
        ),
        icon = BitmapDescriptorFactory.fromResource(R.drawable.agent)
    )
}
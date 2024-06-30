package com.huikka.supertag.ui.components.hud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.huikka.supertag.R
import com.huikka.supertag.data.dto.Zone
import com.huikka.supertag.ui.components.InfoChip

@Composable
fun RunnerHUD(money: Int, currentZone: Zone?, activeZones: List<Zone>) {
    Row(
        modifier = Modifier
            .padding(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp
            )
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CommonHUD(money = money)
        if (currentZone in activeZones) {
            InfoChip(
                text = currentZone!!.name,
                icon = ImageVector.vectorResource(id = R.drawable.location_pin)
            )
        }
    }
}
package com.huikka.supertag.ui.components.hud

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.Config
import com.huikka.supertag.ui.components.Timer
import com.huikka.supertag.ui.state.TimerState

@Composable
fun RunnerTimers(zoneUpdateTime: Long, zonePresenceTimer: TimerState) {
    Timer(
        startTime = zoneUpdateTime,
        totalTime = Config.RUNNER_ZONE_SHUFFLE_TIME,
        handleColor = MaterialTheme.colorScheme.primary,
        inactiveBarColor = Color.Gray,
        activeBarColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(75.dp),
        title = stringResource(id = R.string.zone)
    )
    Spacer(modifier = Modifier.width(16.dp))
    if (zonePresenceTimer.currentTime != 0L) {
        Timer(
            startTime = zonePresenceTimer.currentTime,
            totalTime = zonePresenceTimer.totalTime,
            handleColor = MaterialTheme.colorScheme.primary,
            inactiveBarColor = Color.Gray,
            activeBarColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(75.dp),
            title = stringResource(id = R.string.runner)
        )
    }
}
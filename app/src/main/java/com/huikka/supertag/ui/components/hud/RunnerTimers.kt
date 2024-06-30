package com.huikka.supertag.ui.components.hud

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huikka.supertag.R
import com.huikka.supertag.ui.components.Timer
import com.huikka.supertag.ui.state.TimerState

@Composable
fun RunnerTimers(zoneShuffleTimer: TimerState, zonePresenceTimer: TimerState) {
    Timer(
        currentTime = zoneShuffleTimer.currentTime,
        totalTime = zoneShuffleTimer.totalTime,
        modifier = Modifier.size(75.dp),
        title = stringResource(id = R.string.zone)
    )
    Spacer(modifier = Modifier.width(16.dp))
    if (zonePresenceTimer.isRunning) {
        Timer(
            currentTime = zonePresenceTimer.currentTime,
            totalTime = zonePresenceTimer.totalTime,
            modifier = Modifier.size(75.dp),
            title = stringResource(id = R.string.earning_money)
        )
    }
}
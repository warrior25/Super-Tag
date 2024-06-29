package com.huikka.supertag.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.TimeConverter

@Composable
fun PowerupCard(
    title: String,
    description: String,
    cost: Int,
    enabled: Boolean = true,
    totalTime: Long,
    timeRemaining: Long,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        enabled = enabled
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${stringResource(id = R.string.currency)} $cost",
                modifier = Modifier.padding(16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
        ) {
            val (activeMin, activeS) = TimeConverter.longToMinutesAndSeconds(totalTime)
            Text(text = description, modifier = Modifier.padding(16.dp))
            Text(text = "$activeMin m $activeS s", modifier = Modifier.padding(16.dp))
        }
        if (timeRemaining != 0L) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { timeRemaining / totalTime.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
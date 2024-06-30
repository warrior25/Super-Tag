package com.huikka.supertag.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.huikka.supertag.data.dto.Player

@Composable
fun PlayerListItem(player: Player, isRunner: Boolean, onClick: () -> Unit) {
    var fontWeight = FontWeight.Normal
    var fontSize = 16.sp
    if (isRunner) {
        fontWeight = FontWeight.Bold
        fontSize = 20.sp
    }
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
    ) {
        OutlinedButton(onClick = {
            onClick()
        }) {
            Text(text = player.name, fontWeight = fontWeight, fontSize = fontSize)
        }
    }
}
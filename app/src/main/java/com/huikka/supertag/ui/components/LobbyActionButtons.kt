package com.huikka.supertag.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun LobbyActionButtons(leaveLobby: () -> Unit, openSettings: () -> Unit, isHost: Boolean) {
    IconButton(onClick = { leaveLobby() }) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Leave lobby")
    }
    if (isHost) {
        IconButton(onClick = { openSettings() }) {
            Icon(Icons.Filled.Settings, "Open settings")
        }
    }
}
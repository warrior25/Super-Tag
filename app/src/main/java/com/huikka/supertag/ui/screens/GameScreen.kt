package com.huikka.supertag.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavController
import com.huikka.supertag.LocationUpdateService
import com.huikka.supertag.ui.components.OsmdroidMap
import com.huikka.supertag.ui.events.GameEvent
import com.huikka.supertag.ui.state.GameState
import com.utsman.osmandcompose.rememberOverlayManagerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController, state: GameState, onEvent: (GameEvent) -> Unit
) {
    val overlayManagerState = rememberOverlayManagerState()
    val context = LocalContext.current
    LaunchedEffect(true) {
        val intent = Intent(context, LocationUpdateService::class.java)
        startForegroundService(context, intent)
        //val copyright = CopyrightOverlay(context)
        //overlayManagerState.overlayManager.add(copyright)
    }
    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text("Game")
            }, actions = {

            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OsmdroidMap(overlayManagerState)
        }
    }
}
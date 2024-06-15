package com.huikka.supertag.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huikka.supertag.R
import com.huikka.supertag.ui.components.LobbyActionButtons
import com.huikka.supertag.ui.components.PlayerListItem
import com.huikka.supertag.ui.events.LobbyEvent
import com.huikka.supertag.ui.state.LobbyState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(state: LobbyState, onEvent: (LobbyEvent) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text(state.gameId)
            }, actions = {
                LobbyActionButtons({ onEvent(LobbyEvent.OnLeaveGameClick) },
                    { /*startSettingsActivity()*/ })
            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.players) { player ->
                        PlayerListItem(
                            player = player, isRunner = player.id == state.game!!.runnerId
                        ) {
                            if (state.isHost) {
                                onEvent(LobbyEvent.OnRunnerChange(player.id!!))
                            }
                        }
                    }
                }
            }

            if (state.isHost) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = {
                        onEvent(LobbyEvent.OnStartGameClick)
                        //startGameActivity()
                    }) {
                        Text(stringResource(id = R.string.start_game))
                    }
                }
            }
        }
    }
}
package com.huikka.supertag.ui.screens

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.GameStatuses
import com.huikka.supertag.ui.GameScreenRoute
import com.huikka.supertag.ui.LobbySettingsScreenRoute
import com.huikka.supertag.ui.components.LobbyActionButtons
import com.huikka.supertag.ui.components.PlayerListItem
import com.huikka.supertag.ui.events.LoadingEvent
import com.huikka.supertag.ui.events.LobbyEvent
import com.huikka.supertag.ui.state.LoadingState
import com.huikka.supertag.ui.state.LobbyState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    navController: NavController,
    gameId: String,
    state: LobbyState,
    onEvent: (LobbyEvent) -> Unit,
    loading: LoadingState,
    loadingEvent: (LoadingEvent) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        onEvent(LobbyEvent.OnInit(gameId))
    }
    LaunchedEffect(state.isInitialized, state.game?.status) {
        if (!state.isInitialized) {
            return@LaunchedEffect
        }
        if (state.game?.status == GameStatuses.PLAYING) {
            navController.navigate(GameScreenRoute)
            onEvent(LobbyEvent.OnNavigateAway)
            loadingEvent(
                LoadingEvent.OnUpdateLoadingStatus(true, context.getString(R.string.loading_map)),
            )
        } else if (state.game?.status == GameStatuses.LOBBY) {
            loadingEvent(LoadingEvent.OnUpdateLoadingStatus(false))
        }
    }
    if (loading.loading) {
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text(state.gameId)
            }, actions = {
                LobbyActionButtons({
                    onEvent(LobbyEvent.OnLeaveGameClick)
                    navController.navigateUp()
                }, {
                    navController.navigate(
                        LobbySettingsScreenRoute(
                            state.gameId,
                            state.game!!.headStart!!,
                            state.game.runnerMoney!!,
                            state.game.chaserMoney!!
                        )
                    )
                }, state.isHost)
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
                        Log.d("PLAYER_LIST", player.toString())
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
                    }) {
                        Text(stringResource(id = R.string.start_game))
                    }
                }
            }
        }
    }
}
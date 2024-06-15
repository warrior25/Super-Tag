package com.huikka.supertag.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.GameStatuses
import com.huikka.supertag.ui.LobbyRoute
import com.huikka.supertag.ui.components.FloatingActionButtonWithText
import com.huikka.supertag.ui.events.MainEvent
import com.huikka.supertag.ui.state.MainState

@Composable
fun MainScreen(
    navController: NavController,
    state: MainState,
    onEvent: (MainEvent) -> Unit,
) {
    LaunchedEffect(true) {
        onEvent(MainEvent.OnInit)
    }
    LaunchedEffect(state.gameStatus) {
        when (state.gameStatus) {
            GameStatuses.LOBBY -> {
                navController.navigate(LobbyRoute)
            }

            GameStatuses.PLAYING -> {
                // TODO: Navigate to game screen
            }
        }
    }
    if (state.username == "") {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.loading), fontSize = 22.sp)
        }
        return
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(text = state.username, fontSize = 22.sp)
        FilledTonalButton(onClick = {
            onEvent(MainEvent.OnLogoutClick)
            // TODO: Navigate to login screen
            //val intent = Intent(this@MainActivity, LoginActivity::class.java)
            //startActivity(intent)
        }) {
            Text(stringResource(id = R.string.logout))
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.permissionErrorInfoTextId != null) {
            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = state.permissionErrorInfoTextId),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    // TODO: Request permissions
                    //requestPermissions()
                }) {
                    Text(stringResource(id = state.permissionErrorButtonTextId!!))
                }
            }
            return
        }
        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = state.error, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = state.gameId,
                onValueChange = { gameId ->
                    if (gameId.length <= 6 && (gameId.matches(Regex("^[A-Za-z]+\$")) or gameId.isEmpty())) {
                        onEvent(MainEvent.OnGameIdChange(gameId.uppercase()))
                    }
                },
                label = { Text(stringResource(id = R.string.game_id)) },

                )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onEvent(MainEvent.OnJoinGameClick)
                }, enabled = state.gameId.length == 6
            ) {
                Text(stringResource(id = R.string.join_game))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            FloatingActionButtonWithText(icon = { Icon(Icons.Filled.Add, "Host game") },
                text = { Text(text = stringResource(id = R.string.host_game)) }) {
                onEvent(MainEvent.OnHostGameClick)
            }
        }
    }
}
package com.huikka.supertag.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huikka.supertag.R
import com.huikka.supertag.ui.components.SettingsSlider
import com.huikka.supertag.ui.events.LobbySettingsEvent
import com.huikka.supertag.ui.state.LobbySettingsState
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbySettingsScreen(
    navController: NavController,
    gameId: String,
    headStart: Int,
    runnerMoney: Int,
    chaserMoney: Int,
    state: LobbySettingsState,
    onEvent: (LobbySettingsEvent) -> Unit
) {
    LaunchedEffect(true) {
        onEvent(LobbySettingsEvent.OnSettingsReset(headStart, runnerMoney, chaserMoney))
    }
    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text(stringResource(id = R.string.game_settings))
            }, navigationIcon = {
                IconButton(onClick = {
                    onEvent(LobbySettingsEvent.OnSave(gameId))
                    navController.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    onEvent(
                        LobbySettingsEvent.OnSettingsReset(
                            Random.nextInt(1, 30),
                            Random.nextInt(0, 300),
                            Random.nextInt(0, 300)
                        )
                    )
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.dice),
                        contentDescription = "Randomize"
                    )
                }
                IconButton(onClick = {
                    onEvent(
                        LobbySettingsEvent.OnSettingsReset(
                            headStart, runnerMoney, chaserMoney
                        )
                    )
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_restart_alt_24),
                        contentDescription = "Reset"
                    )
                }
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
                    .padding(top = 32.dp, start = 32.dp, end = 32.dp)
            ) {
                SettingsSlider(
                    value = state.headStart,
                    onValueChange = { onEvent(LobbySettingsEvent.OnHeadStartChange(it)) },
                    valueRange = 1f..30f,
                    title = stringResource(id = R.string.head_start),
                    unit = stringResource(id = R.string.minutes)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSlider(
                    value = state.runnerMoney,
                    onValueChange = { onEvent(LobbySettingsEvent.OnRunnerMoneyChange(it)) },
                    valueRange = 0f..300f,
                    title = stringResource(id = R.string.runner_money),
                    unit = stringResource(id = R.string.currency)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSlider(
                    value = state.chaserMoney,
                    onValueChange = { onEvent(LobbySettingsEvent.OnChaserMoneyChange(it)) },
                    valueRange = 0f..300f,
                    title = stringResource(id = R.string.chaser_money),
                    unit = stringResource(id = R.string.currency)
                )
            }
        }
    }
}
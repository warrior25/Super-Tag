package com.huikka.supertag.ui.components.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.huikka.supertag.ui.state.CardState

@Composable
fun ChaserCards(cardStates: List<CardState>, card1action: () -> Unit) {
    PowerupCard(
        title = stringResource(id = cardStates[0].titleResId),
        description = stringResource(id = cardStates[0].descriptionResId),
        cost = cardStates[0].cost,
        enabled = cardStates[0].enabled,
        totalTime = cardStates[0].timerState.totalTime,
        timeRemaining = cardStates[0].timerState.currentTime
    ) {
        card1action()
    }
}
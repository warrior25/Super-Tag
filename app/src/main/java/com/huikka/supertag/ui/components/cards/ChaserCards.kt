package com.huikka.supertag.ui.components.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.huikka.supertag.ui.state.CardState

@Composable
fun ChaserCards(cardStates: List<CardState>, cardActions: (Int) -> Unit) {
    cardStates.forEachIndexed { index, cardState ->
        PowerupCard(
            title = stringResource(id = cardState.titleResId),
            description = stringResource(id = cardState.descriptionResId),
            cost = cardState.cost,
            enabled = cardState.enabled,
            totalTime = cardState.timerState.totalTime,
            timeRemaining = cardState.timerState.currentTime
        ) {
            cardActions(index)
        }
    }
}
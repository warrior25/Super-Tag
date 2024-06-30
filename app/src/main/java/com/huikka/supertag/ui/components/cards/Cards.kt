package com.huikka.supertag.ui.components.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.huikka.supertag.data.helpers.Side
import com.huikka.supertag.ui.state.CardState

@Composable
fun Cards(cardStates: List<CardState>, cardActions: (Int) -> Unit, side: Side) {
    cardStates.forEachIndexed { index, cardState ->
        if (cardState.side == side) {
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
}
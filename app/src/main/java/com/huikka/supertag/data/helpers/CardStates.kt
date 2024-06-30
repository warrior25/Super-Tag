package com.huikka.supertag.data.helpers

import com.huikka.supertag.R
import com.huikka.supertag.ui.state.CardState
import com.huikka.supertag.ui.state.TimerState

val cards = listOf(
    CardState(
        titleResId = R.string.card1_title,
        descriptionResId = R.string.card1_description,
        cost = 100,
        timerState = TimerState(totalTime = 5 * minute),
        side = Side.Chasers
    ), CardState(
        titleResId = R.string.card2_title,
        descriptionResId = R.string.card2_description,
        cost = 200,
        timerState = TimerState(totalTime = 5 * minute),
        side = Side.Runner
    ), CardState(
        titleResId = R.string.card3_title,
        descriptionResId = R.string.card3_description,
        cost = 300,
        timerState = TimerState(totalTime = 5 * minute),
        side = Side.Runner
    )
)
package com.huikka.supertag.ui.state

import com.huikka.supertag.data.helpers.Side

data class CardState(
    val titleResId: Int,
    val descriptionResId: Int,
    val cost: Int,
    val enabled: Boolean = false,
    val activeUntil: Long? = null,
    val timeRemaining: Long = 0,
    val timerState: TimerState = TimerState(),
    val side: Side
)
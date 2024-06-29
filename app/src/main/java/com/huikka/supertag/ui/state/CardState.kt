package com.huikka.supertag.ui.state

data class CardState(
    val titleResId: Int,
    val descriptionResId: Int,
    val cost: Int,
    val enabled: Boolean = false,
    val activeUntil: Long? = null,
    val timeRemaining: Long = 0,
    val timerState: TimerState = TimerState()
)
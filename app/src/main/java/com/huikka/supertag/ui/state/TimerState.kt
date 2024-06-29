package com.huikka.supertag.ui.state

import android.os.CountDownTimer

data class TimerState(
    val timer: CountDownTimer? = null,
    val currentTime: Long = 0,
    val totalTime: Long = 0,
    val isRunning: Boolean = false,
    val isCancelled: Boolean = false
)
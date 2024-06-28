package com.huikka.supertag.ui.state

data class MainState(
    val gameId: String = "",
    val gameStatus: String? = null,
    val username: String = "",
    val error: String = "",
    val isInitialized: Boolean = false
)
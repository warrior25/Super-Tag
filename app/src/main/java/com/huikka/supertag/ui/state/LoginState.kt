package com.huikka.supertag.ui.state

data class LoginState(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val isRegistering: Boolean = false,
    val error: Int? = null,
    val isLoggedIn: Boolean = false,
    val isInitialized: Boolean = false,
    val ongoingAction: Boolean = false
)
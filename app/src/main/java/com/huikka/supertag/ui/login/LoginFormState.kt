package com.huikka.supertag.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
        val usernameError: Int? = null,
        val passwordError: Int? = null,
        val nicknameError: Int? = null,
        val isDataValid: Boolean = false
)
package com.huikka.supertag.ui.events

sealed class LoginEvent {
    data object OnLogin : LoginEvent()
    data object OnRegister : LoginEvent()
    data class OnEmailChange(val email: String) : LoginEvent()
    data class OnPasswordChange(val password: String) : LoginEvent()
    data class OnNicknameChange(val nickname: String) : LoginEvent()
    data object OnLogout : LoginEvent()
    data object CheckLoginStatus : LoginEvent()
    data object OnModeChange : LoginEvent()
}
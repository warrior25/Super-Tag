package com.huikka.supertag.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.huikka.supertag.R
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao
import com.huikka.supertag.ui.events.LoginEvent
import com.huikka.supertag.ui.state.LoginState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authDao: AuthDao
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLogin -> login()
            is LoginEvent.OnRegister -> register()
            is LoginEvent.OnNicknameChange -> updateNickname(event.nickname)
            is LoginEvent.OnEmailChange -> updateEmail(event.email)
            is LoginEvent.OnPasswordChange -> updatePassword(event.password)
            is LoginEvent.OnLogout -> logout()
            is LoginEvent.CheckLoginStatus -> checkLoginStatus()
            is LoginEvent.OnModeChange -> changeFormType()
        }
    }

    private fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update {
                    it.copy(
                        ongoingAction = true
                    )
                }
                authDao.login(state.value.email, state.value.password)
                _state.update {
                    it.copy(
                        isLoggedIn = true, ongoingAction = false
                    )
                }
                delay(1000)
                resetState()
            } catch (e: Exception) {
                Log.e("LoginViewModel", "failed to login: $e")
                _state.update {
                    it.copy(
                        error = R.string.login_failed, ongoingAction = false
                    )
                }
            }
        }
    }

    private fun register() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update {
                    it.copy(
                        ongoingAction = true
                    )
                }
                authDao.register(state.value.email, state.value.password, state.value.nickname)
                _state.update {
                    it.copy(
                        isLoggedIn = true, ongoingAction = false
                    )
                }
                delay(1000)
                resetState()
            } catch (e: Exception) {
                Log.e("LoginViewModel", "failed to register: $e")
                _state.update {
                    it.copy(
                        error = R.string.register_failed, ongoingAction = false
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authDao.logout()
                _state.update {
                    it.copy(
                        isLoggedIn = false, isInitialized = true
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to logout: $e")
                _state.update {
                    it.copy(
                        error = R.string.logout_failed
                    )
                }
            }
        }
    }

    private fun resetState() {
        _state.update {
            it.copy(
                isInitialized = false,
                email = "",
                password = "",
                nickname = "",
                isRegistering = false,
            )
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = authDao.awaitCurrentSession()
            if (session?.user != null) {
                _state.update {
                    it.copy(
                        isLoggedIn = true, isInitialized = true
                    )
                }
                delay(1000)
                resetState()
            } else {
                _state.update {
                    it.copy(
                        isLoggedIn = false, isInitialized = true
                    )
                }
            }
        }
    }

    private fun changeFormType() {
        _state.update {
            it.copy(
                isRegistering = !it.isRegistering
            )
        }
    }

    private fun updateNickname(nickname: String) {
        _state.update {
            it.copy(
                nickname = nickname
            )
        }
    }

    private fun updateEmail(email: String) {
        _state.update {
            it.copy(
                email = email
            )
        }
    }

    private fun updatePassword(password: String) {
        _state.update {
            it.copy(
                password = password
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Get the Application object from extras
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val myApp = application as STApplication

                return LoginViewModel(
                    myApp.authDao
                ) as T
            }
        }
    }
}
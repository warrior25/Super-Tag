package com.huikka.supertag.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.huikka.supertag.R
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dao.AuthDao

class LoginViewModel(application: STApplication) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val authDao = AuthDao(application)

    suspend fun login(email: String, password: String) {
        // can be launched in a separate asynchronous job
        val loggedIn = authDao.login(email, password)

        if (loggedIn) {
            val displayName = authDao.getUser()!!.userMetadata?.get("nickname").toString()
            val userId = authDao.getUser()!!.id
            _loginResult.value = LoginResult(success = LoggedInUserView(displayName, userId))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    suspend fun register(nickname: String, email: String, password: String) {
        val registered = authDao.register(email, password, nickname)

        if (registered) {
            val displayName = authDao.getUser()!!.userMetadata?.get("nickname").toString()
            val userId = authDao.getUser()!!.id
            _loginResult.value = LoginResult(success = LoggedInUserView(displayName, userId))
        } else {
            _loginResult.value = LoginResult(error = R.string.register_failed)
        }
    }

    fun loginDataChanged(
        isRegisterForm: Boolean,
        nickname: String,
        email: String,
        password: String
    ) {
        if (!isNickNameValid(nickname) && isRegisterForm) {
            _loginForm.value = LoginFormState(nicknameError = R.string.invalid_nickname)
        }
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isNickNameValid(nickname: String): Boolean {
        return nickname.isNotBlank()
    }

    // A placeholder username validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
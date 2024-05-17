package com.huikka.supertag.ui.login

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.huikka.supertag.R
import com.huikka.supertag.STApplication
import com.huikka.supertag.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val nickname = binding.nickname!!
        val createAccount = binding.createAccount!!
        val alreadyAccount = binding.alreadyAccount!!
        val title = binding.loginViewTitle!!

        loginViewModel = ViewModelProvider(
            this, LoginViewModelFactory(application as STApplication)
        )[LoginViewModel::class.java]

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login/register button unless inputs are valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
            if (loginState.nicknameError != null) {
                nickname.error = getString(loginState.nicknameError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            } else if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
                //Complete and destroy login activity once successful
                finish()
            }
        })

        nickname.afterTextChanged {
            loginViewModel.loginDataChanged(
                isRegisterForm(),
                nickname.text.toString(),
                email.text.toString(),
                password.text.toString()
            )
        }

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                isRegisterForm(),
                nickname.text.toString(),
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    isRegisterForm(),
                    nickname.text.toString(),
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> CoroutineScope(Dispatchers.Main).launch {
                        loginViewModel.login(
                            email.text.toString(), password.text.toString()
                        )
                    }
                }
                false
            }

            login.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    loading.visibility = View.VISIBLE
                    if (nickname.visibility == View.VISIBLE) {
                        loginViewModel.register(
                            nickname.text.toString(),
                            email.text.toString(),
                            password.text.toString()
                        )
                    } else {
                        loginViewModel.login(email.text.toString(), password.text.toString())
                    }
                }
            }


            alreadyAccount.setOnClickListener {
                nickname.visibility = View.GONE
                alreadyAccount.visibility = View.GONE
                createAccount.visibility = View.VISIBLE
                val loginText = getString(R.string.action_login)
                title.text = loginText
                login.text = loginText
            }

            createAccount.setOnClickListener {
                nickname.visibility = View.VISIBLE
                alreadyAccount.visibility = View.VISIBLE
                createAccount.visibility = View.GONE
                val loginText = getString(R.string.action_register)
                title.text = loginText
                login.text = loginText
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        Toast.makeText(
            applicationContext, "$welcome $displayName", Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun isRegisterForm(): Boolean {
        return binding.nickname?.visibility == View.VISIBLE
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
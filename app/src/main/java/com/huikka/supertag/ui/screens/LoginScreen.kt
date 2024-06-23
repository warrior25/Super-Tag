package com.huikka.supertag.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huikka.supertag.R
import com.huikka.supertag.ui.LoginScreenRoute
import com.huikka.supertag.ui.MainScreenRoute
import com.huikka.supertag.ui.events.LoginEvent
import com.huikka.supertag.ui.state.LoginState

@Composable
fun LoginScreen(
    navController: NavController,
    logout: Boolean,
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
) {
    LaunchedEffect(Unit) {
        if (logout && state.isLoggedIn) {
            Log.d("LoginScreen", "logout")
            onEvent(LoginEvent.OnLogout)
        } else {
            Log.d("LoginScreen", "check login")
            onEvent(LoginEvent.CheckLoginStatus)
        }
    }

    LaunchedEffect(state.isInitialized, state.isLoggedIn) {
        if (state.isInitialized && state.isLoggedIn) {
            Log.d("LoginScreen", "navigate to main screen")
            navController.navigate(MainScreenRoute) {
                popUpTo(LoginScreenRoute()) { inclusive = true }
            }
        }
    }
    if (!state.isInitialized) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.loading), fontSize = 22.sp)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(state.error ?: R.string.empty), color = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = state.email,
            onValueChange = { email ->
                onEvent(LoginEvent.OnEmailChange(email))
            },
            label = { Text(stringResource(id = R.string.prompt_email)) },
            isError = !state.email.matches(
                Regex(android.util.Patterns.EMAIL_ADDRESS.pattern())
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        if (!state.isRegistering) {
            TextField(
                value = state.password,
                onValueChange = { password ->
                    onEvent(LoginEvent.OnPasswordChange(password))
                },
                label = { Text(stringResource(id = R.string.prompt_password)) },
                isError = !state.password.matches(
                    Regex("^.+$")
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                onEvent(LoginEvent.OnLogin)
            }, enabled = !state.ongoingAction) {
                Text(stringResource(id = R.string.action_login))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                onEvent(LoginEvent.OnModeChange)
            }) {
                Text(stringResource(id = R.string.new_account))
            }
        } else {
            TextField(
                value = state.password,
                onValueChange = { password ->
                    onEvent(LoginEvent.OnPasswordChange(password))
                },
                label = { Text(stringResource(id = R.string.prompt_password)) },
                isError = !state.password.matches(
                    Regex("^.{6,}$")
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = state.nickname,
                onValueChange = { nickname ->
                    onEvent(LoginEvent.OnNicknameChange(nickname))
                },
                label = { Text(stringResource(id = R.string.prompt_nickname)) },
                isError = !state.nickname.matches(
                    Regex("^.{6,}$")
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                onEvent(LoginEvent.OnRegister)
            }, enabled = !state.ongoingAction) {
                Text(stringResource(id = R.string.action_register))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                onEvent(LoginEvent.OnModeChange)
            }) {
                Text(stringResource(id = R.string.already_account))
            }
        }
    }
}
package com.huikka.supertag.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.huikka.supertag.ui.screens.LobbyScreen
import com.huikka.supertag.ui.screens.LoginScreen
import com.huikka.supertag.ui.screens.MainScreen
import com.huikka.supertag.viewModels.LobbyViewModel
import com.huikka.supertag.viewModels.LoginViewModel
import com.huikka.supertag.viewModels.MainViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    mainViewModel: MainViewModel, lobbyViewModel: LobbyViewModel, loginViewModel: LoginViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginScreenRoute()) {
        composable<LoginScreenRoute> {
            val args = it.toRoute<LoginScreenRoute>()
            val state by loginViewModel.state.collectAsState()
            LoginScreen(
                navController = navController,
                logout = args.logout,
                state = state,
                onEvent = loginViewModel::onEvent
            )
        }
        composable<MainScreenRoute> {
            val state by mainViewModel.state.collectAsState()
            MainScreen(
                navController = navController, state = state, onEvent = mainViewModel::onEvent
            )
        }
        composable<LobbyScreenRoute> {
            val args = it.toRoute<LobbyScreenRoute>()
            val state by lobbyViewModel.state.collectAsState()
            LobbyScreen(
                navController = navController,
                gameId = args.gameId,
                state = state,
                onEvent = lobbyViewModel::onEvent
            )
        }
    }
}

@Serializable
data class LoginScreenRoute(
    val logout: Boolean = false
)

@Serializable
object MainScreenRoute

@Serializable
data class LobbyScreenRoute(
    val gameId: String
)
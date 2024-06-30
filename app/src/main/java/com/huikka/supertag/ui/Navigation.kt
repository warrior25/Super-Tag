package com.huikka.supertag.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.huikka.supertag.ui.screens.GameScreen
import com.huikka.supertag.ui.screens.LobbyScreen
import com.huikka.supertag.ui.screens.LobbySettingsScreen
import com.huikka.supertag.ui.screens.LoginScreen
import com.huikka.supertag.ui.screens.MainScreen
import com.huikka.supertag.ui.screens.PermissionErrorScreen
import com.huikka.supertag.viewModels.GameViewModel
import com.huikka.supertag.viewModels.LoadingViewModel
import com.huikka.supertag.viewModels.LobbySettingsViewModel
import com.huikka.supertag.viewModels.LobbyViewModel
import com.huikka.supertag.viewModels.LoginViewModel
import com.huikka.supertag.viewModels.MainViewModel
import com.huikka.supertag.viewModels.PermissionErrorViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    permissionErrorViewModel: PermissionErrorViewModel,
    mainViewModel: MainViewModel,
    lobbyViewModel: LobbyViewModel,
    lobbySettingsViewModel: LobbySettingsViewModel,
    loginViewModel: LoginViewModel,
    gameViewModel: GameViewModel,
    loadingViewModel: LoadingViewModel,
) {

    val navController = rememberNavController()
    val loading by loadingViewModel.state.collectAsState()


    NavHost(navController = navController, startDestination = PermissionErrorScreenRoute) {
        composable<PermissionErrorScreenRoute> {
            val state by permissionErrorViewModel.state.collectAsState()
            PermissionErrorScreen(
                navController = navController,
                state = state,
                onEvent = permissionErrorViewModel::onEvent
            )
        }
        composable<LoginScreenRoute> {
            val args = it.toRoute<LoginScreenRoute>()
            val state by loginViewModel.state.collectAsState()
            LoginScreen(
                navController = navController,
                logout = args.logout,
                state = state,
                onEvent = loginViewModel::onEvent,
                loading = loading,
                loadingEvent = loadingViewModel::onEvent

            )
        }
        composable<MainScreenRoute> {
            val state by mainViewModel.state.collectAsState()
            MainScreen(
                navController = navController,
                state = state,
                onEvent = mainViewModel::onEvent,
                loading = loading,
                loadingEvent = loadingViewModel::onEvent
            )
        }
        composable<LobbyScreenRoute> {
            val args = it.toRoute<LobbyScreenRoute>()
            val state by lobbyViewModel.state.collectAsState()
            LobbyScreen(
                navController = navController,
                gameId = args.gameId,
                state = state,
                onEvent = lobbyViewModel::onEvent,
                loading = loading,
                loadingEvent = loadingViewModel::onEvent
            )
        }
        composable<LobbySettingsScreenRoute> {
            val args = it.toRoute<LobbySettingsScreenRoute>()
            val state by lobbySettingsViewModel.state.collectAsState()
            LobbySettingsScreen(
                navController = navController,
                gameId = args.gameId,
                headStart = args.headStart,
                runnerMoney = args.runnerMoney,
                chaserMoney = args.chaserMoney,
                state = state,
                onEvent = lobbySettingsViewModel::onEvent
            )
        }
        composable<GameScreenRoute> {
            val state by gameViewModel.state.collectAsState()
            GameScreen(
                navController = navController,
                state = state,
                onEvent = gameViewModel::onEvent,
                loading = loading,
                loadingEvent = loadingViewModel::onEvent
            )
        }
    }
}

@Serializable
object PermissionErrorScreenRoute

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

@Serializable
data class LobbySettingsScreenRoute(
    val gameId: String, val headStart: Int, val runnerMoney: Int, val chaserMoney: Int
)

@Serializable
object GameScreenRoute
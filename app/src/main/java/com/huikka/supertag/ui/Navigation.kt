package com.huikka.supertag.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.huikka.supertag.ui.screens.LobbyScreen
import com.huikka.supertag.ui.screens.MainScreen
import com.huikka.supertag.viewModels.LobbyViewModel
import com.huikka.supertag.viewModels.MainViewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigation(mainViewModel: MainViewModel, lobbyViewModel: LobbyViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> {
            val state by mainViewModel.state.collectAsState()
            MainScreen(
                navController = navController, state = state, onEvent = mainViewModel::onEvent
            )
        }
        composable<LobbyRoute> {
            val state by lobbyViewModel.state.collectAsState()
            LobbyScreen(state = state, onEvent = lobbyViewModel::onEvent)
        }
    }
}

@Serializable
object MainScreenRoute

@Serializable
object LobbyRoute
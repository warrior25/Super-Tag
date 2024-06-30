package com.huikka.supertag.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huikka.supertag.R
import com.huikka.supertag.data.helpers.PermissionError
import com.huikka.supertag.ui.LoginScreenRoute
import com.huikka.supertag.ui.events.PermissionErrorEvent
import com.huikka.supertag.ui.state.PermissionErrorState

@Composable
fun PermissionErrorScreen(
    navController: NavController,
    state: PermissionErrorState,
    onEvent: (PermissionErrorEvent) -> Unit
) {
    LaunchedEffect(state.permissionError) {
        if (state.permissionError == null) {
            navController.navigate(LoginScreenRoute())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.permissionError == PermissionError.NotRequested) {
            Text(
                text = stringResource(id = R.string.insufficient_permissions),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                onEvent(PermissionErrorEvent.OnRequestPermissions)
            }) {
                Text(stringResource(id = R.string.fix_now))
            }
        } else if (state.permissionError == PermissionError.Denied) {
            Text(
                text = stringResource(id = R.string.permissions_denied),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                onEvent(PermissionErrorEvent.OnRequestPermissions)
            }) {
                Text(stringResource(id = R.string.open_settings))
            }
        }
    }
}
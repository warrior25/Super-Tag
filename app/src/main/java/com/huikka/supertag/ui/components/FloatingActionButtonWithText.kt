package com.huikka.supertag.ui.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable

@Composable
fun FloatingActionButtonWithText(
    icon: @Composable () -> Unit, text: @Composable () -> Unit, onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = { onClick() },
        icon = icon,
        text = text,
    )
}
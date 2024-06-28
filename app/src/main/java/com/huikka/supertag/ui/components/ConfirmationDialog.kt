package com.huikka.supertag.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ConfirmationDialog(
    text: String,
    title: String,
    icon: ImageVector,
    confirmText: String,
    dismissText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(title = { Text(text = title) },
        text = { Text(text = text) },
        icon = { Icon(icon, contentDescription = "Leave game icon") },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = dismissText)
            }
        })
}
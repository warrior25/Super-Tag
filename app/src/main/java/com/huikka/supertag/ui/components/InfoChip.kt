package com.huikka.supertag.ui.components

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoChip(text: String, icon: ImageVector) {
    ElevatedAssistChip(onClick = { },
        label = { Text(text = text, fontSize = 22.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon, contentDescription = "Info icon"
            )
        },
        elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 10.dp)
    )
}
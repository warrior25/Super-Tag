package com.huikka.supertag.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoneyChip(
    targetNumber: Int = 0, icon: ImageVector
) {
    // Animate the number from 0 (or any starting number) to targetNumber
    val animatedNumber by animateIntAsState(
        targetValue = targetNumber, label = ""
    )

    ElevatedAssistChip(onClick = { },
        label = { Text(text = "$animatedNumber", fontSize = 22.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon, contentDescription = "Info icon"
            )
        },
        elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 10.dp)
    )
}
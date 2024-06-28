package com.huikka.supertag.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SettingsSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    title: String,
    unit: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, fontWeight = FontWeight.Bold, fontSize = 22.sp
        )
        Text(text = "$value $unit", fontSize = 22.sp)
    }
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.roundToInt()) },
        valueRange = valueRange,
    )
}
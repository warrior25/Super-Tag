package com.huikka.supertag.ui.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.huikka.supertag.R
import com.huikka.supertag.ui.components.MoneyChip

@Composable
fun CommonHUD(money: Int) {
    MoneyChip(
        targetNumber = money, icon = ImageVector.vectorResource(id = R.drawable.dollar_sign)
    )
}
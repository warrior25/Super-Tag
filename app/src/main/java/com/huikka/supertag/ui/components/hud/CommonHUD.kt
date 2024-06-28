package com.huikka.supertag.ui.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.huikka.supertag.R
import com.huikka.supertag.ui.components.InfoChip

@Composable
fun CommonHUD(money: Int) {
    InfoChip(
        text = money.toString(), icon = ImageVector.vectorResource(id = R.drawable.dollar_sign)
    )
}
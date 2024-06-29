package com.huikka.supertag.data.helpers

import com.huikka.supertag.R
import com.huikka.supertag.ui.state.CardState

val cards = listOf(
    CardState(
        titleResId = R.string.card1_title,
        descriptionResId = R.string.card1_description,
        cost = 100,
        activeTime = 5 * minute
    )
)
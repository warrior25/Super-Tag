package com.huikka.supertag.ui.state

data class CardState(
    val titleResId: Int,
    val descriptionResId: Int,
    val cost: Int,
    val enabled: Boolean = false,
    val activeTime: Long,
    val activeUntil: Long? = null
)
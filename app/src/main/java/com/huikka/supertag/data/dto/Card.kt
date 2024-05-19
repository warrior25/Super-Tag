package com.huikka.supertag.data.dto


data class Card(
    val id: String,
    val name: String,
    val price: Int,
    val icon: Int,
    val duration: Int = -1,
    val disabled: Boolean = false
)
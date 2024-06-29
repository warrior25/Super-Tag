package com.huikka.supertag.data.helpers

object ZoneType {
    const val ATTRACTION = "attraction"
    const val PLAYING_AREA = "playing_area"
    const val STORE = "store"
    const val ATM = "atm"
    val RUNNER_ZONE_TYPES = listOf(ATTRACTION)
    val CHASER_ZONE_TYPES = listOf(STORE, ATM)
}
package com.huikka.supertag.data.model

data class Player(
    val id: String,
    val name: String,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val locationAccuracy: Float? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val icon: String = "default",
) {
    // No-arg constructor for deserialization
    constructor() : this(
        id = "",
        name = ""
    )
}

package com.huikka.supertag.ui.state

import com.huikka.supertag.data.helpers.PermissionErrors

data class MainState(
    val gameId: String = "",
    val gameStatus: String? = null,
    val username: String = "",
    val error: String = "",
    val permissionErrorButtonTextId: Int? = null,
    val permissionErrorInfoTextId: Int? = null,
    val permissionError: PermissionErrors? = null,
    val isInitialized: Boolean = false
)
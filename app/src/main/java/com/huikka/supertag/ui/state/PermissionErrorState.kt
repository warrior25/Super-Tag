package com.huikka.supertag.ui.state

import com.huikka.supertag.data.helpers.PermissionErrors

data class PermissionErrorState(
    val permissionError: PermissionErrors? = null, val permissionsRequested: Boolean = false
)

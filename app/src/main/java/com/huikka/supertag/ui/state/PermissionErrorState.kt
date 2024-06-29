package com.huikka.supertag.ui.state

import com.huikka.supertag.data.helpers.PermissionError

data class PermissionErrorState(
    val permissionError: PermissionError? = null, val permissionsRequested: Boolean = false
)

package com.huikka.supertag.ui.events

sealed class PermissionErrorEvent {
    data object OnRequestPermissions : PermissionErrorEvent()
}
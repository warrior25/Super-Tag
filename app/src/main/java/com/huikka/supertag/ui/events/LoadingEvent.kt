package com.huikka.supertag.ui.events

sealed class LoadingEvent {
    data class OnUpdateLoadingStatus(val loading: Boolean, val text: String = "") : LoadingEvent()
}
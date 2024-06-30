package com.huikka.supertag.viewModels

import androidx.lifecycle.ViewModel
import com.huikka.supertag.ui.events.LoadingEvent
import com.huikka.supertag.ui.state.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoadingViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoadingState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoadingEvent) {
        when (event) {
            is LoadingEvent.OnUpdateLoadingStatus -> updateLoading(event.loading, event.text)
        }
    }

    private fun updateLoading(boolean: Boolean, text: String) {
        _state.update {
            it.copy(
                loading = boolean, text = text
            )
        }
    }
}
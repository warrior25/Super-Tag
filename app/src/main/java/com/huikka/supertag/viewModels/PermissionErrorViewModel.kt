package com.huikka.supertag.viewModels

import androidx.lifecycle.ViewModel
import com.huikka.supertag.data.helpers.PermissionError
import com.huikka.supertag.ui.events.PermissionErrorEvent
import com.huikka.supertag.ui.state.PermissionErrorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PermissionErrorViewModel : ViewModel() {
    private val _state = MutableStateFlow(PermissionErrorState())
    val state = _state.asStateFlow()

    fun onEvent(event: PermissionErrorEvent) {
        when (event) {
            is PermissionErrorEvent.OnRequestPermissions -> requestPermissions()
        }
    }

    fun updatePermissionError(permissionError: PermissionError?) {
        _state.update {
            it.copy(
                permissionError = permissionError
            )
        }
    }

    private fun requestPermissions() {
        _state.update {
            it.copy(
                permissionsRequested = true
            )
        }
    }

    fun resetPermissionsRequested() {
        _state.update {
            it.copy(
                permissionsRequested = false
            )
        }
    }

}
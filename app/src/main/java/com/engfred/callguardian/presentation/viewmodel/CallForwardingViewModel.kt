package com.engfred.callguardian.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallForwardingViewModel : ViewModel() {

    private val _forwardingNumber = MutableStateFlow("")
    val forwardingNumber: StateFlow<String> = _forwardingNumber.asStateFlow()

    private val _forwardAll = MutableStateFlow(false)
    val forwardAll: StateFlow<Boolean> = _forwardAll.asStateFlow()

    private val _forwardWhenBusy = MutableStateFlow(false)
    val forwardWhenBusy: StateFlow<Boolean> = _forwardWhenBusy.asStateFlow()

    private val _forwardWhenUnanswered = MutableStateFlow(false)
    val forwardWhenUnanswered: StateFlow<Boolean> = _forwardWhenUnanswered.asStateFlow()

    private val _forwardWhenUnreachable = MutableStateFlow(false)
    val forwardWhenUnreachable: StateFlow<Boolean> = _forwardWhenUnreachable.asStateFlow()

    fun updateForwardingNumber(number: String) {
        _forwardingNumber.value = number
    }

    fun toggleForwardAll() {
        _forwardAll.value = !_forwardAll.value
    }

    fun toggleForwardWhenBusy() {
        _forwardWhenBusy.value = !_forwardWhenBusy.value
    }

    fun toggleForwardWhenUnanswered() {
        _forwardWhenUnanswered.value = !_forwardWhenUnanswered.value
    }

    fun toggleForwardWhenUnreachable() {
        _forwardWhenUnreachable.value = !_forwardWhenUnreachable.value
    }

    fun resetState() {
        _forwardingNumber.value = ""
        _forwardAll.value = false
        _forwardWhenBusy.value = false
        _forwardWhenUnanswered.value = false
        _forwardWhenUnreachable.value = false
    }
}

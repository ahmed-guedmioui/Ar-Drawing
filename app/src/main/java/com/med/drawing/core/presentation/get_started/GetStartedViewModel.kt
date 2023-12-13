package com.med.drawing.core.presentation.get_started

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
@HiltViewModel
class GetStartedViewModel @Inject constructor() : ViewModel() {

    private val _getStartedState = MutableStateFlow(GetStartedState())
    val getsStartedState = _getStartedState.asStateFlow()

    fun onEvent(getStartedUiEvent: GetStartedUiEvent) {
        when (getStartedUiEvent) {
            GetStartedUiEvent.ShowHidePrivacyDialog -> {
                _getStartedState.update {
                    it.copy(showPrivacyDialog = !getsStartedState.value.showPrivacyDialog)
                }
            }
        }
    }
}



























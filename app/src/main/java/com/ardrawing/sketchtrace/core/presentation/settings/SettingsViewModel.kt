package com.ardrawing.sketchtrace.core.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    fun onEvent(settingsUiEvent: SettingsUiEvent) {
        when (settingsUiEvent) {
            SettingsUiEvent.ShowHidePrivacyDialog -> {
                _settingsState.update {
                    it.copy(showPrivacyDialog = !settingsState.value.showPrivacyDialog)
                }
            }
        }
    }

}



























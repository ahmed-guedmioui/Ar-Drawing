package com.med.drawing.main.presentaion.settings

import androidx.lifecycle.ViewModel
import com.med.drawing.splash.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()


    init {
    }

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



























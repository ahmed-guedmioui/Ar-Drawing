package com.med.drawing.core.presentation.settings

/**
 * @author Ahmed Guedmioui
 */
sealed interface SettingsUiEvent {
    object ShowHidePrivacyDialog : SettingsUiEvent
}
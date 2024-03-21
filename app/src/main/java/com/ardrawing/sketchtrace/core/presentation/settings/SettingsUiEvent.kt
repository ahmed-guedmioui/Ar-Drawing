package com.ardrawing.sketchtrace.core.presentation.settings

/**
 * @author Ahmed Guedmioui
 */
sealed interface SettingsUiEvent {
    object ShowHidePrivacyDialog : SettingsUiEvent
}
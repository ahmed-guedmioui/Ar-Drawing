package com.ardrawing.sketchtrace.main.presentaion.settings

/**
 * @author Ahmed Guedmioui
 */
sealed interface SettingsUiEvent {
    object ShowHidePrivacyDialog : SettingsUiEvent
}
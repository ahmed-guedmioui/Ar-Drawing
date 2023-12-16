package com.med.drawing.main.presentaion.settings

/**
 * @author Ahmed Guedmioui
 */
sealed interface SettingsUiEvent {
    object ShowHidePrivacyDialog : SettingsUiEvent
}
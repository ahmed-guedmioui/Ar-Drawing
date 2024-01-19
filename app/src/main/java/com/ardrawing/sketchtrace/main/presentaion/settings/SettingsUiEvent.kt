package com.ardrawing.sketchtrace.main.presentaion.settings

import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
sealed interface SettingsUiEvent {
    object ShowHidePrivacyDialog : SettingsUiEvent

    data class Subscribe(
        val isSubscribed: Boolean,
        val date: Date? = null
    ) : SettingsUiEvent
}
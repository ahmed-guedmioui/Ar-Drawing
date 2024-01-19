package com.ardrawing.sketchtrace.main.presentaion.get_started

import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent
    data class Subscribe(
        val isSubscribed: Boolean,
        val date: Date? = null
    ) : GetStartedUiEvent
}
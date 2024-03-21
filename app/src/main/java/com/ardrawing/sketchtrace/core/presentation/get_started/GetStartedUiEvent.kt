package com.ardrawing.sketchtrace.core.presentation.get_started

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent

}
package com.ardrawing.sketchtrace.main.presentaion.get_started

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent

}
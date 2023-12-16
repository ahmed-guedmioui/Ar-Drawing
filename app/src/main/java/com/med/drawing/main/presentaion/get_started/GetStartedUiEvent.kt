package com.med.drawing.main.presentaion.get_started

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent
}
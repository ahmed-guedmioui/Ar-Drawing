package com.med.drawing.core.presentation.get_started

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent
}
package com.med.drawing.core.presentation.get_started

/**
 * @author Ahmed Guedmioui
 */
sealed interface GetStartedUiEvent {
    object ShowHidePrivacyDialog : GetStartedUiEvent
}
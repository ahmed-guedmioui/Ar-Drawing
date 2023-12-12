package com.med.drawing.core.presentation

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
}
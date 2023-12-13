package com.med.drawing.core.presentation.splash

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
}
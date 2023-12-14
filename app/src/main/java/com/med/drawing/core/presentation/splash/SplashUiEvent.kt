package com.med.drawing.core.presentation.splash

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
}
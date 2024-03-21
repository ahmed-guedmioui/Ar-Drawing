package com.ardrawing.sketchtrace.core.presentation.splash

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
    object ContinueApp : SplashUiEvent

    object AlreadySubscribed : SplashUiEvent
}
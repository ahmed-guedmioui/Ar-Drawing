package com.ardrawing.sketchtrace.splash.presentation.splash

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
    object ContinueApp : SplashUiEvent

    object AlreadySubscribed : SplashUiEvent
}
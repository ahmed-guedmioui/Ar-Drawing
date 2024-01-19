package com.ardrawing.sketchtrace.splash.presentation.splash

import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
sealed interface SplashUiEvent {
    object TryAgain : SplashUiEvent
    object ContinueApp : SplashUiEvent

    object AlreadySubscribed : SplashUiEvent

    data class Subscribe(
        val isSubscribed: Boolean,
        val date: Date? = null
    ) : SplashUiEvent
}
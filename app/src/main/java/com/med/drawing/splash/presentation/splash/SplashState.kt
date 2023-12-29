package com.med.drawing.splash.presentation.splash


/**
 * @author Ahmed Guedmioui
 */
data class SplashState(
    val areImagesLoaded: Boolean = false,
    val isAppDataLoaded: Boolean = false,
    val areBothLoadedChannelAlreadySent: Boolean = false,
)
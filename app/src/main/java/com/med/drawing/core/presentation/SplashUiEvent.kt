package com.med.drawing.core.presentation

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
sealed interface SplashUiEvent {
    data class Paginate(val category: String) : SplashUiEvent
    object Navigate : SplashUiEvent
}
package com.med.drawing.core.presentation.home

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
}
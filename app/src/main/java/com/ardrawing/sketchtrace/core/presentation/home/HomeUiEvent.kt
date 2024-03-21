package com.ardrawing.sketchtrace.core.presentation.home

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
    object ShowHideHelperDialog : HomeUiEvent
}
package com.ardrawing.sketchtrace.main.presentaion.home

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
    object ShowHideHelperDialog : HomeUiEvent
}
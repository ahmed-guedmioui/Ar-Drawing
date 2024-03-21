package com.ardrawing.sketchtrace.core.presentation.tips

/**
 * @author Ahmed Guedmioui
 */
sealed interface TipsUiEvent {
    object NextTip : TipsUiEvent
    object Back : TipsUiEvent
}
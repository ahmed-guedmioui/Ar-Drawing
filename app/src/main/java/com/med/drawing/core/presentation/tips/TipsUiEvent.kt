package com.med.drawing.core.presentation.tips

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
sealed interface TipsUiEvent {
    object NextTip : TipsUiEvent
}
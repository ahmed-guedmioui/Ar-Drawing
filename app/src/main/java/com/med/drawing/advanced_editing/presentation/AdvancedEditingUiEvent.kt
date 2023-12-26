package com.med.drawing.advanced_editing.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface AdvancedEditingUiEvent {
    data class Select(val selected: Int) : AdvancedEditingUiEvent

    data class SetEdge(val edge: Float) : AdvancedEditingUiEvent
    data class SetContrast(val contrast: Float) : AdvancedEditingUiEvent
    data class SetNoise(val noise: Float) : AdvancedEditingUiEvent
    data class SetSharpness(val sharpness: Float) : AdvancedEditingUiEvent
}
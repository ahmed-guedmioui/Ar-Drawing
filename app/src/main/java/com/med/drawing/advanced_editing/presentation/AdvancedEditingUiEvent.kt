package com.med.drawing.advanced_editing.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface AdvancedEditingUiEvent {
    data class Select(val selected: Int) : AdvancedEditingUiEvent

    data class SetEdge(val edge: Int) : AdvancedEditingUiEvent
    data class SetContrast(val contrast: Int) : AdvancedEditingUiEvent
    data class SetNoise(val noise: Int) : AdvancedEditingUiEvent
    data class SetSharpness(val sharpness: Int) : AdvancedEditingUiEvent
}
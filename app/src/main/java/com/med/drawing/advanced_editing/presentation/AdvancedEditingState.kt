package com.med.drawing.advanced_editing.presentation


/**
 * @author Ahmed Guedmioui
 */
data class AdvancedEditingState(
    var selected: Int = 0,

    var edge: Float = 0f,
    var contrast: Float = 0f,
    var noise: Float = 0f,
    var sharpness: Float = 0f,
)
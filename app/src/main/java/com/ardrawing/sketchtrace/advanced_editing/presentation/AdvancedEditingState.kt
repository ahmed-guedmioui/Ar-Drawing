package com.ardrawing.sketchtrace.advanced_editing.presentation


/**
 * @author Ahmed Guedmioui
 */
data class AdvancedEditingState(
    var selected: Int = 0,

    var edge: Int = 0,
    var contrast: Int = 0,
    var noise: Int = 0,
    var sharpness: Int = 0,

    var isEdged: Boolean = false,
    var isContrast: Boolean = false,
    var isNoise: Boolean = false,
    var isSharpened: Boolean = false
)
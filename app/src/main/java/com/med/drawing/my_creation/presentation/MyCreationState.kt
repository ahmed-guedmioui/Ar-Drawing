package com.med.drawing.my_creation.presentation

import com.med.drawing.my_creation.domian.model.Creation


/**
 * @author Ahmed Guedmioui
 */
data class MyCreationState(
    val creationList: List<Creation> = emptyList()
)
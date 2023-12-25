package com.med.drawing.my_creation.presentation.my_creation_list

import com.med.drawing.my_creation.domian.model.Creation


/**
 * @author Ahmed Guedmioui
 */
data class MyCreationListState(
    val creationList: List<Creation> = emptyList()
)
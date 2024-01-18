package com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list

import com.ardrawing.sketchtrace.my_creation.domian.model.Creation


/**
 * @author Ahmed Guedmioui
 */
data class MyCreationListState(
    val creationList: List<Creation> = emptyList()
)
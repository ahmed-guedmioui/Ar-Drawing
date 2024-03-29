package com.ardrawing.sketchtrace.image_list.presentation.categories

import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.my_creation.domian.model.Creation


/**
 * @author Ahmed Guedmioui
 */
data class CategoriesState(
    val isTrace: Boolean = false,
    val isGallery: Boolean = false,

    val imageCategoryList: List<ImageCategory> = emptyList(),

    val imagePosition: Int = 0,
    val clickedImageItem: Image? = null,
    val imageCategory: ImageCategory? = null
)
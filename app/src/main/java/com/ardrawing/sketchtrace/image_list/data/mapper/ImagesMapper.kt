package com.ardrawing.sketchtrace.image_list.data.mapper

import com.ardrawing.sketchtrace.image_list.data.remote.respond.images.ImageCategoryListDto
import com.ardrawing.sketchtrace.image_list.data.remote.respond.images.ImageDto
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory

/**
 * @author Ahmed Guedmioui
 */

fun ImageCategoryListDto.toImageCategoryList(): List<ImageCategory> {
    var currentCategoryId = 1 // Start category ID from 1
    return category_list?.map { categoryDto ->
        ImageCategory(
            categoryId = currentCategoryId,
            imageCategoryName = categoryDto.category_name.orEmpty(),
            imageList = categoryDto.images?.map { it.toImage(currentCategoryId) } ?: emptyList()
        ).also { currentCategoryId++ }
    } ?: emptyList()
}

fun ImageDto.toImage(currentCategoryId: Int): Image {
    return Image(
        prefsId = "${currentCategoryId}_${id}",
        id = id ?: 0,
        image = image.orEmpty(),
        locked = locked ?: false
    )
}

object Index {
    private var currentCategoryId = -1
    val categoryId: Int
        get() = currentCategoryId++
}











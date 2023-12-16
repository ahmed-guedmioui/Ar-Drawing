package com.med.drawing.image_list.data.mapper

import com.med.drawing.image_list.data.remote.respond.images.ImageDto
import com.med.drawing.image_list.data.remote.respond.images.ImageListDto
import com.med.drawing.image_list.domain.model.images.Image
import com.med.drawing.image_list.domain.model.images.ImageList

/**
 * @author Ahmed Guedmioui
 */
fun ImageDto.toImage(): Image {
    return Image(
        category_name ?: "",
        id ?: 0,
        image ?: "",
        locked ?: false
    )
}

fun ImageListDto.toImageList(): ImageList {
    return ImageList(
        images = images?.map { imageDtoList ->
            imageDtoList.map { it.toImage() }
        } ?: emptyList()
    )
}
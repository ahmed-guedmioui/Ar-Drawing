package com.med.drawing.image_list.data.remote.respond.images

data class CategoryDto(
    val category_name: String?,
    val category_id: Int?,
    val images: List<ImageDto>?
)
package com.med.drawing.image_list.data.remote.respond.images

data class ImageDto(
    val category_name: String?,
    val id: Int?,
    val image: String?,
    val locked: Boolean?
)
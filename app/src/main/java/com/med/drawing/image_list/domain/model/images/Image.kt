package com.med.drawing.image_list.domain.model.images

data class Image(
    val category_name: String,
    val id: Int,
    val image: String,
    val locked: Boolean
)
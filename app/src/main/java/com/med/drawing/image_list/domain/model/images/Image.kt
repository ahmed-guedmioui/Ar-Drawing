package com.med.drawing.image_list.domain.model.images

data class Image(
    val prefsId: String,
    val id: Int,
    val image: String,
    var locked: Boolean
)
package com.med.drawing.image_list.domain.model.images

import androidx.recyclerview.widget.RecyclerView
import com.med.drawing.image_list.presentation.adapter.CategoryAdapter

data class ImageCategory(
    val imageCategoryName: String,
    val categoryId: Int,
    val imageList: List<Image>,

    var adapter: CategoryAdapter? = null,
    var recyclerView: RecyclerView? = null
)
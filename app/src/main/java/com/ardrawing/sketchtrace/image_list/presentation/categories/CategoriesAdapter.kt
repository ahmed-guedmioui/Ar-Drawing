package com.ardrawing.sketchtrace.image_list.presentation.categories

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.image_list.presentation.category.CategoryAdapter
import com.ardrawing.sketchtrace.util.ads.NativeManager

/**
 * @author Ahmed Guedmioui
 */
class CategoriesAdapter(
    private val imageCategoryList: List<ImageCategory>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(i: Int): Int {
        return when (imageCategoryList[i].imageCategoryName) {
            "native" -> 0
            "gallery and camera" -> 1
            "explore" -> 2
            else -> 3
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.include_native, parent, false)
            return NativeViewHolder(view, activity)
        }

        if (viewType == 1) {
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.item_from_gallery_camera, parent, false)
            return GalleryAndCameraViewHolder(view)
        }

        if (viewType == 2) {
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.item_explore, parent, false)
            return ExploreViewHolder(view)
        }

        val view: View = LayoutInflater.from(activity)
            .inflate(R.layout.item_category, parent, false)
        return CategoriesViewHolder(view, activity)

    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder, categoryPosition: Int
    ) {

        if (imageCategoryList[categoryPosition].imageCategoryName == "native") {
            return
        }


        if (imageCategoryList[categoryPosition].imageCategoryName == "gallery and camera") {
            val holder = viewHolder as GalleryAndCameraViewHolder
            holder.gallery.setOnClickListener {
                galleryAndCameraClickListener.oClick(true)
            }
            holder.camera.setOnClickListener {
                galleryAndCameraClickListener.oClick(false)
            }
            return
        }

        if (imageCategoryList[categoryPosition].imageCategoryName == "explore") {
            return
        }

        val holder = viewHolder as CategoriesViewHolder

        val categoryAdapter = CategoryAdapter(
            activity, imageCategoryList[categoryPosition], 1
        )

        categoryAdapter.setClickListener(object : CategoryAdapter.ClickListener {
            override fun oClick(imagePosition: Int) {
                clickListener.oClick(categoryPosition, imagePosition)
            }

        })
        holder.categoryName.text =
            imageCategoryList[categoryPosition].imageCategoryName

        holder.categoryRecyclerView.adapter = categoryAdapter
        imageCategoryList[categoryPosition].adapter = categoryAdapter
        imageCategoryList[categoryPosition].recyclerView = holder.categoryRecyclerView

        holder.viewMore.setOnClickListener {
            viewMoreClickListener.oClick(categoryPosition)
        }
    }


    override fun getItemCount(): Int {
        return if (imageCategoryList.isNotEmpty())
            imageCategoryList.size
        else 0

    }

    private class CategoriesViewHolder(itemView: View, activity: Activity) :
        RecyclerView.ViewHolder(itemView) {
        var categoryRecyclerView: RecyclerView
        var categoryName: TextView
        var viewMore: TextView

        init {
            categoryRecyclerView = itemView.findViewById(R.id.category_recycler_view)
            viewMore = itemView.findViewById(R.id.view_more)
            categoryName = itemView.findViewById(R.id.category_name)
            categoryRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

            categoryRecyclerView.hasFixedSize()
        }
    }

    private class NativeViewHolder(itemView: View, activity: Activity) :
        RecyclerView.ViewHolder(itemView) {
        init {
            NativeManager.loadNative(
                itemView.findViewById(R.id.native_frame),
                itemView.findViewById(R.id.native_temp),
                activity, false
            )
        }
    }

    private class GalleryAndCameraViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var gallery: CardView
        var camera: CardView

        init {
            gallery = itemView.findViewById(R.id.gallery)
            camera = itemView.findViewById(R.id.camera)
        }
    }

    private class ExploreViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
    }

    private lateinit var viewMoreClickListener: ViewMoreClickListener
    fun setViewMoreClickListener(viewMoreClickListener: ViewMoreClickListener) {
        this.viewMoreClickListener = viewMoreClickListener
    }

    interface ViewMoreClickListener {
        fun oClick(categoryPosition: Int)
    }


    private lateinit var clickListener: ClickListener
    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun oClick(categoryPosition: Int, imagePosition: Int)
    }


    private lateinit var galleryAndCameraClickListener: GalleryAndCameraClickListener
    fun setGalleryAndCameraClickListener(
        galleryAndCameraClickListener: GalleryAndCameraClickListener
    ) {
        this.galleryAndCameraClickListener = galleryAndCameraClickListener
    }

    interface GalleryAndCameraClickListener {
        fun oClick(isGallery: Boolean)
    }

}

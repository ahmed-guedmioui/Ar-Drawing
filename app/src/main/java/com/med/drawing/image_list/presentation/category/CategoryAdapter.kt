package com.med.drawing.image_list.presentation.category

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.med.drawing.R
import com.med.drawing.image_list.domain.model.images.ImageCategory

/**
 * @author Ahmed Guedmioui
 */
class CategoryAdapter(
    val activity: Activity,
    private val category: ImageCategory,
    private val from: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (category.imageList[position].locked) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // When coming from categoriesAdapter that is 1:
        // The inflated layout needs to be 150dp width because we use a horizontal recyclerView
        return if (from == 1) {
            if (viewType == 0) {
                val view: View = LayoutInflater.from(activity)
                    .inflate(R.layout.item_image_horizontal_locked, parent, false)
                return CategoryViewHolder(view)
            }
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.item_image_horizontal, parent, false)
            CategoryViewHolder(view)
        } else {

            // When coming from viewMoreActivity that is 0:
            // The inflated layout needs to fill parent width because we use a gridLayoutManager
            if (viewType == 0) {
                val view: View = LayoutInflater.from(activity)
                    .inflate(R.layout.item_image_locked, parent, false)
                return CategoryViewHolder(view)
            }
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.item_image, parent, false)
            CategoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, imagePosition: Int) {
        val holder: CategoryViewHolder = viewHolder as CategoryViewHolder

        Glide.with(activity)
            .load(category.imageList[imagePosition].image)
            .thumbnail(0.25f)
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable?>, isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable?>?,
                    dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }
            }).into(holder.image)

        holder.image.setOnClickListener {
            clickListener.oClick(imagePosition)
        }
    }

    override fun getItemCount(): Int {
        return category.imageList.size
    }

    private class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var progressBar: ProgressBar

        init {
            image = itemView.findViewById(R.id.image)
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }

    private lateinit var clickListener: ClickListener

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun oClick(imagePosition: Int)
    }

}

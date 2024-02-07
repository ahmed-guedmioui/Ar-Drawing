package com.ardrawing.sketchtrace.paywall.presentation

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ardrawing.sketchtrace.R


/**
 * @author Ahmed Guedmioui
 */
class ImagesViewPagerAdapter(
    private val activity: Activity,
    private val images: List<Drawable?>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = activity.layoutInflater.inflate(
            R.layout.item_image_step, parent, false
        )

        return ReviewsViewHolder(itemView)
    }

    private inner class ReviewsViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            val image = itemView.findViewById<ImageView>(R.id.image_step)
            image.setImageDrawable(images[position])
        }
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ReviewsViewHolder).bind(position)
    }
}











package com.ardrawing.sketchtrace.paywall.presentation

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ardrawing.sketchtrace.R

/**
 * @author Ahmed Guedmioui
 */
class ReviewsViewPagerAdapter(
    private val activity: Activity,
    private val reviews: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView =  activity.layoutInflater.inflate(
            R.layout.item_review, parent, false
        )

        return ReviewsViewHolder(itemView)
    }

    private inner class ReviewsViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            val review = itemView.findViewById<TextView>(R.id.review)
            review.text = reviews[position]
        }
    }

    override fun getItemCount(): Int = reviews.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ReviewsViewHolder).bind(position)
    }
}











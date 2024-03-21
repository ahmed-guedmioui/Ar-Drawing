package com.ardrawing.sketchtrace.core.presentation.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ardrawing.sketchtrace.R


/**
 * @author Ahmed Guedmioui
 */
class HelperPagerAdapter(private val context: Context) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout: View = when (position) {
            0 -> inflater.inflate(R.layout.include_sketch_helper, container, false)
            1 -> inflater.inflate(R.layout.include_trace_helper, container, false)
            else -> throw IllegalArgumentException("Invalid position")
        }

        container.addView(layout)
        return layout
    }

    override fun getCount(): Int {
        return 2
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }


    companion object {
        private const val MAX_HEIGHT = 500 // Set your desired maximum height
    }
}
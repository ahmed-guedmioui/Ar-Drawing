package com.med.drawing.my_creation.presentation.my_creation_list.adapter

import android.app.Activity
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.med.drawing.R
import com.med.drawing.my_creation.domian.model.Creation

/**
 * @author Ahmed Guedmioui
 */
class MyCreationListAdapter(
    val activity: Activity,
    private val creationList: List<Creation>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (creationList[position].isVideo) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 0) {
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.item_creation_video, parent, false)
            return CreationViewHolder(view)
        }

        val view: View = LayoutInflater.from(activity)
            .inflate(R.layout.item_creation_photo, parent, false)
        return CreationViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val holder: CreationViewHolder = viewHolder as CreationViewHolder
        val uri = creationList[position].uri

        if (creationList[position].isVideo) {

            try {
                val contentResolver = activity.contentResolver
                val fileDescriptor = contentResolver
                    .openFileDescriptor(uri, "r")?.fileDescriptor ?: return

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(fileDescriptor)

                val bitmap = retriever.frameAtTime
                retriever.release()

                holder.image.setImageBitmap(bitmap)

            } catch (e: Exception) {
            }

            holder.image.setOnClickListener {
                clickListener.oClick(uri.toString(), true)
            }
            return
        }

        Glide.with(activity)
            .load(uri)
            .thumbnail(0.25f)
            .into(holder.image)

        holder.image.setOnClickListener {
            clickListener.oClick(uri.toString(), false)
        }
    }

    override fun getItemCount(): Int {
        return creationList.size
    }

    private class CreationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView

        init {
            image = itemView.findViewById(R.id.image)
        }
    }

    private lateinit var clickListener: ClickListener

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun oClick(uri: String, isVideo: Boolean)
    }

}

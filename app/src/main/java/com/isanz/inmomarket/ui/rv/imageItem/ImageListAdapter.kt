package com.isanz.inmomarket.ui.rv.imageItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.isanz.inmomarket.R

class ImageListAdapter :
    ListAdapter<String, ImageListAdapter.ImageViewHolder>(ImageDiffCallback()) {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivImage)
        val delButton: ImageButton = itemView.findViewById(R.id.ibDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.images_item, parent, false)
        return ImageViewHolder(view)
    }


    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.itemView).load(getItem(position)).into(holder.image)

        holder.delButton.setOnClickListener {
            // Get the current position of the item
            val currentPosition = holder.adapterPosition

            // Check if the position is valid
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Remove the item from the list
                submitList(currentList.filterIndexed { index, _ -> index != currentPosition })
            }
        }
    }


}
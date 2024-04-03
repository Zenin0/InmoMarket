package com.isanz.inmomarket.ui.rv.imageItem

import androidx.recyclerview.widget.DiffUtil

class ImageDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        // Return true if items are the same.
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        // Return true if contents are the same.
        return oldItem == newItem
    }
}
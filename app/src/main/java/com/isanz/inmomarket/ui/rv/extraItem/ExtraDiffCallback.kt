package com.isanz.inmomarket.ui.rv.extraItem

import androidx.recyclerview.widget.DiffUtil

class ExtraDiffCallback : DiffUtil.ItemCallback<Pair<String, Int>>() {
    override fun areItemsTheSame(oldItem: Pair<String, Int>, newItem: Pair<String, Int>): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Pair<String, Int>,
        newItem: Pair<String, Int>
    ): Boolean {
        return oldItem == newItem
    }
}
package com.isanz.inmomarket.ui.rv.parcelaItem

import androidx.recyclerview.widget.DiffUtil
import com.isanz.inmomarket.utils.entities.Property

class PropertyDiffCallback<T> : DiffUtil.ItemCallback<Property>() {

    override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
        return oldItem == newItem
    }
}
package com.isanz.inmomarket.ui.rv.parcelaItem

import androidx.recyclerview.widget.DiffUtil
import com.isanz.inmomarket.utils.entities.Parcela

class ParcelaDiffCallback<T> : DiffUtil.ItemCallback<Parcela>() {

    override fun areItemsTheSame(oldItem: Parcela, newItem: Parcela): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Parcela, newItem: Parcela): Boolean {
        return oldItem == newItem
    }
}
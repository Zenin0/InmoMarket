package com.isanz.inmomarket.rv.extraItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isanz.inmomarket.R

class ExtraListAdapter(private val fragmentType: String) :
    ListAdapter<Pair<String, Int>, ExtraListAdapter.ExtraViewHolder>(ExtraDiffCallback()) {

    class ExtraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ivExtra)
        val cuantity: TextView = itemView.findViewById(R.id.tvExtra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_extra, parent, false)
        return ExtraViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExtraViewHolder, position: Int) {
        val item = getItem(position)
        holder.cuantity.text = item.second.toString()

        val color = when (fragmentType) {
            "HomeFragment" -> android.R.color.white
            "PropertyFragment" -> android.R.color.black
            else -> android.R.color.white
        }

        holder.cuantity.setTextColor(ContextCompat.getColor(holder.cuantity.context, color))

        val layoutParams = holder.icon.layoutParams
        holder.icon.layoutParams = layoutParams

        if (item.first == "rooms") {
            holder.icon.setImageResource(R.drawable.ic_bedroom)
        }
        if (item.first == "baths") {
            holder.icon.setImageResource(R.drawable.ic_bathroom)
        }
        if (item.first == "floors") {
            holder.icon.setImageResource(R.drawable.ic_house_siding)
        }
        if (item.first == "squareMeters") {
            holder.icon.setImageResource(R.drawable.ic_square_foot)
        }

        holder.icon.setColorFilter(ContextCompat.getColor(holder.icon.context, color))
    }
}
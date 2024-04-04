package com.isanz.inmomarket.ui.rv.parcelaItem

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Parcela

class ParcelaListAdapter :
    ListAdapter<Parcela, ParcelaListAdapter.ParcelaViewHolder>((ParcelaDiffCallback<Parcela>())) {

    class ParcelaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.parcela_image)
        val tittle: TextView = itemView.findViewById(R.id.tvParcela)
        val view: View = itemView.findViewById(R.id.vOverlay)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParcelaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parcela_item, parent, false)
        return ParcelaViewHolder(view)
    }


    override fun onBindViewHolder(holder: ParcelaViewHolder, position: Int) {
        val parcela = getItem(position)
        holder.tittle.text = parcela.tittle

        holder.view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Remove the listener to ensure it's only called once
                holder.view.viewTreeObserver.removeOnPreDrawListener(this)

                // Now the sizes are available
                Log.i(TAG, holder.view.measuredWidth.toString() + " : " + holder.view.measuredHeight.toString())

                Glide.with(holder.image.context)
                    .load(parcela.listImagesUri[1])
                    .override(holder.view.measuredWidth, holder.view.measuredHeight)
                    .centerCrop()
                    .into(holder.image)

                return true
            }
        })
    }


}
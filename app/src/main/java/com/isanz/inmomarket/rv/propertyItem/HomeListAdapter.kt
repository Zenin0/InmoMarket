package com.isanz.inmomarket.rv.propertyItem

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.isanz.inmomarket.R
import com.isanz.inmomarket.rv.extraItem.ExtraListAdapter
import com.isanz.inmomarket.utils.entities.Property
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener

class HomeListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Property, HomeListAdapter.PropertyViewHolder>((HomeDiffCallback<Property>())) {

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)
    }

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val self = itemView
        val image: ImageView = itemView.findViewById(R.id.property_image)
        val tittle: TextView = itemView.findViewById(R.id.tvProperty)
        val view: View = itemView.findViewById(R.id.vOverlay)
        val rvExtras: RecyclerView = itemView.findViewById(R.id.rvExtras)
        val btnFav: ImageView = itemView.findViewById(R.id.ibAddFavorite)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return PropertyViewHolder(view)
    }


    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)
        holder.tittle.text = property.tittle

        setUpButtons(holder, property)
        loadExtras(holder, property)
        setUpItems(holder, property)
    }

    private fun setUpItems(holder: PropertyViewHolder, property: Property) {
        holder.view.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Remove the listener to ensure it's only called once
                holder.view.viewTreeObserver.removeOnPreDrawListener(this)

                // Now the sizes are available
                Log.i(
                    TAG,
                    holder.view.measuredWidth.toString() + " : " + holder.view.measuredHeight.toString()
                )

                val radiusInDp = 16
                val scale = holder.image.context.resources.displayMetrics.density
                val radiusInPx = (radiusInDp * scale + 0.5f).toInt()

                Glide.with(holder.image.context).load(property.listImagesUri[0])
                    .override(holder.view.measuredWidth, holder.view.measuredHeight)
                    .transform(RoundedCorners(radiusInPx)).into(holder.image)

                return true
            }
        })
    }


    private fun setUpButtons(holder: PropertyViewHolder, property: Property) {

        val updateFavoriteIcon: (Boolean) -> Unit = { isFavorite ->
            if (isFavorite) {
                holder.btnFav.setImageResource(R.drawable.ic_favorite)
            } else {
                holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        // Call the getIfFavorite function from the ViewModel
        viewModel.getIfFavorite(property, updateFavoriteIcon)

        // Load the animation
        val rotateAnimation = AnimationUtils.loadAnimation(holder.btnFav.context, R.anim.rotate)

        holder.btnFav.setOnClickListener {
            // Start the animation
            it.startAnimation(rotateAnimation)

            viewModel.alterFavorite(property, updateFavoriteIcon)
        }

        holder.self.setOnClickListener {
            this.listener.onItemClicked(property.id!!)
        }
    }

    private fun loadExtras(holder: PropertyViewHolder, property: Property?) {
        // Create and set the adapter for the inner RecyclerView
        val extraAdapter = ExtraListAdapter("HomeFragment")
        holder.rvExtras.adapter = extraAdapter

        // Convert the Map to a List of Pairs and submit it to the adapter
        property?.extras?.let { extras ->
            val extrasList = extras.toList()
            extraAdapter.submitList(extrasList)
        }
    }


}
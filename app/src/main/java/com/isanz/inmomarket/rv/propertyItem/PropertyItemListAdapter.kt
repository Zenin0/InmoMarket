package com.isanz.inmomarket.rv.propertyItem

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.OneShotPreDrawListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.isanz.inmomarket.R
import com.isanz.inmomarket.rv.extraItem.ExtraListAdapter
import com.isanz.inmomarket.utils.entities.Property
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener

class PropertyItemListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Property, PropertyItemListAdapter.PropertyViewHolder>((PropertyItemDiffCallback())) {

    private val viewModel: PropertyItemViewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(PropertyItemViewModel::class.java)
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
        setUpImage(holder, property)
    }

    private fun setUpImage(holder: PropertyViewHolder, property: Property) {
        OneShotPreDrawListener.add(holder.image) {
            loadImage(holder, property)
        }
    }

    private fun loadImage(holder: PropertyViewHolder, property: Property) {
        val radiusInDp = 16
        val scale = holder.image.context.resources.displayMetrics.density
        val radiusInPx = (radiusInDp * scale + 0.5f).toInt()
        Glide.with(holder.image.context).load(property.listImagesUri[0])
            .override(holder.view.measuredWidth, holder.view.measuredHeight)
            .transform(RoundedCorners(radiusInPx)).into(holder.image)
    }


    private fun setUpButtons(holder: PropertyViewHolder, property: Property) {

        val updateFavoriteIcon: (Boolean) -> Unit = { isFavorite ->
            if (isFavorite) {
                holder.btnFav.setImageResource(R.drawable.ic_favorite)
            } else {
                holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        viewModel.getIfFavorite(property, updateFavoriteIcon)
        val rotateAnimation = AnimationUtils.loadAnimation(holder.btnFav.context, R.anim.rotate)
        holder.btnFav.setOnClickListener {
            it.startAnimation(rotateAnimation)
            viewModel.alterFavorite(property, updateFavoriteIcon)
        }

        holder.self.setOnClickListener {
            this.listener.onItemClicked(property.id!!)
        }
    }


    fun attachToRecyclerView(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val icon: Drawable? = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_delete_forever)
            private val background = ColorDrawable(Color.argb(128, 255, 200, 200))

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val property = getItem(position)
                viewModel.deleteProperty(property)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2

                if (dX < 0) {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    icon.setBounds(itemView.right - iconMargin - icon.intrinsicWidth, itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)
                    icon.draw(c)
                } else {
                    background.setBounds(0, 0, 0, 0)
                    icon.setBounds(0, 0, 0, 0)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun loadExtras(holder: PropertyViewHolder, property: Property?) {
        val extraAdapter = ExtraListAdapter("HomeFragment")
        holder.rvExtras.adapter = extraAdapter
        property?.extras?.let { extras ->
            val extrasList = extras.toList()
            extraAdapter.submitList(extrasList)
        }
    }


}
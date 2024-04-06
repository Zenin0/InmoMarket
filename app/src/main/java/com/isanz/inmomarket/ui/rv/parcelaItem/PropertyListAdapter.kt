package com.isanz.inmomarket.ui.rv.parcelaItem

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.ui.rv.extraItem.ExtraListAdapter
import com.isanz.inmomarket.utils.entities.Property

class PropertyListAdapter :
    ListAdapter<Property, PropertyListAdapter.PropertyViewHolder>((PropertyDiffCallback<Property>())) {


    private val db = FirebaseFirestore.getInstance()
    private val user = InmoMarket.getAuth().currentUser

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.property_image)
        val tittle: TextView = itemView.findViewById(R.id.tvProperty)
        val view: View = itemView.findViewById(R.id.vOverlay)
        val rvExtras: RecyclerView = itemView.findViewById(R.id.rvExtras)
        val btnFav: ImageView = itemView.findViewById(R.id.ibAddFavorite)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.property_item, parent, false)
        return PropertyViewHolder(view)
    }


    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)
        holder.tittle.text = property.tittle

        if (user != null) {
            val docRef = db.collection("properties").document(property.id!!)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val favorites = document.get("favorites") as? List<*>
                    if (favorites != null && favorites.contains(user.uid)) {
                        // If the user's ID is already in the favorites array, set the favorite icon
                        holder.btnFav.setImageResource(R.drawable.ic_favorite)
                    } else {
                        // If the user's ID is not in the favorites array, set the non-favorite icon
                        holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
                    }
                }
            }
        }

        holder.btnFav.setOnClickListener {
            alterFavorite(holder, property)
        }

        loadExtras(holder, property)

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

    private fun alterFavorite(holder: PropertyViewHolder, property: Property?) {
        val docRef = db.collection("properties").document(property!!.id!!)
        docRef.get().addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? List<*>
            if (favorites != null && favorites.contains(user!!.uid)) {
                // If the user's ID is already in the favorites array, remove it
                docRef.update("favorites", FieldValue.arrayRemove(user.uid)).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(
                        holder.btnFav.context, "Removed from favorites!", Toast.LENGTH_SHORT
                    ).show()
                    holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(
                        holder.btnFav.context, "Error removing from favorites", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // If the user's ID is not in the favorites array, add it
                docRef.update("favorites", FieldValue.arrayUnion(user!!.uid)).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(
                        holder.btnFav.context, "Added to favorites!", Toast.LENGTH_SHORT
                    ).show()
                    holder.btnFav.setImageResource(R.drawable.ic_favorite)
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(
                        holder.btnFav.context, "Error adding to favorites", Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }

    private fun loadExtras(holder: PropertyViewHolder, property: Property?) {
        // Create and set the adapter for the inner RecyclerView
        val extraAdapter = ExtraListAdapter()
        holder.rvExtras.adapter = extraAdapter

        // Convert the Map to a List of Pairs and submit it to the adapter
        property?.extras?.let { extras ->
            val extrasList = extras.toList()
            extraAdapter.submitList(extrasList)
        }
    }


}
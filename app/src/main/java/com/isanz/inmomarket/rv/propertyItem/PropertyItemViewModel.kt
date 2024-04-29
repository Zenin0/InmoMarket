package com.isanz.inmomarket.rv.propertyItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class PropertyItemViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val user = InmoMarket.getAuth().currentUser


    fun getIfFavorite(property: Property, callback: (Boolean) -> Unit) {
        val docRef = db.collection("properties").document(property.id!!)
        docRef.get().addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? List<*>
            if (favorites != null && favorites.contains(user!!.uid)) {
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun alterFavorite(property: Property, updateFavoriteIcon: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docRef = db.collection("properties").document(property.id!!)
            docRef.get().addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<*>
                if (favorites != null && favorites.contains(user!!.uid)) {
                    updateFavoriteIcon(false)
                    docRef.update("favorites", FieldValue.arrayRemove(user.uid))
                        .addOnFailureListener {
                            updateFavoriteIcon(true)
                        }
                } else {
                    updateFavoriteIcon(true)

                    docRef.update("favorites", FieldValue.arrayUnion(user!!.uid))
                        .addOnFailureListener {
                            updateFavoriteIcon(false)
                        }
                }
            }
        }
    }

    fun deleteProperty(property: Property) {
        db.collection("properties").document(property.id!!).delete()
    }
}
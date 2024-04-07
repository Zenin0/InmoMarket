package com.isanz.inmomarket.ui.rv.propertyItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class PropertyViewModel : ViewModel() {

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

    fun alterFavorite(property: Property) {
        viewModelScope.launch {
            val docRef = db.collection("properties").document(property.id!!)
            docRef.get().addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<*>
                if (favorites != null && favorites.contains(user!!.uid)) {
                    // If the user's ID is already in the favorites array, remove it
                    docRef.update("favorites", FieldValue.arrayRemove(user.uid))
                } else {
                    // If the user's ID is not in the favorites array, add it
                    docRef.update("favorites", FieldValue.arrayUnion(user!!.uid))
                }
            }
        }
    }
}
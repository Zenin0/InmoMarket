package com.isanz.inmomarket.rv.propertyItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class PropertyItemViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val user = InmoMarket.getAuth().currentUser

    fun getIfFavorite(property: Property, callback: (Boolean) -> Unit) {
        val docRef = getDocumentReference(property)
        docRef.get().addOnSuccessListener { document ->
            val isFavorite = checkIfFavorite(document.get("favorites") as? List<*>)
            callback(isFavorite)
        }
    }

    private fun getDocumentReference(property: Property) = db.collection("properties").document(property.id!!)

    private fun checkIfFavorite(favorites: List<*>?) = favorites != null && favorites.contains(user!!.uid)

    fun alterFavorite(property: Property, updateFavoriteIcon: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docRef = getDocumentReference(property)
            docRef.get().addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<*>
                if (checkIfFavorite(favorites)) {
                    removeFavorite(docRef, updateFavoriteIcon)
                } else {
                    addFavorite(docRef, updateFavoriteIcon)
                }
            }
        }
    }

    private fun removeFavorite(docRef: DocumentReference, updateFavoriteIcon: (Boolean) -> Unit) {
        updateFavoriteIcon(false)
        docRef.update("favorites", FieldValue.arrayRemove(user!!.uid))
            .addOnFailureListener {
                updateFavoriteIcon(true)
            }
    }

    private fun addFavorite(docRef: DocumentReference, updateFavoriteIcon: (Boolean) -> Unit) {
        updateFavoriteIcon(true)
        docRef.update("favorites", FieldValue.arrayUnion(user!!.uid))
            .addOnFailureListener {
                updateFavoriteIcon(false)
            }
    }

    fun deleteProperty(property: Property) {
        getDocumentReference(property).delete()
    }
}
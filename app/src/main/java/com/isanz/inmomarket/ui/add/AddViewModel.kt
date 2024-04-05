package com.isanz.inmomarket.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun save(
        tittle: String,
        description: String,
        rooms: Int,
        baths: Int,
        location: String,
        images: List<String>
    ): String {
        var result = ""
        val property = hashMapOf(
            "tittle" to tittle,
            "description" to description,
            "rooms" to rooms,
            "baths" to baths,
            "location" to location,
            "listImagesUri" to images,
            "userId" to InmoMarket.getAuth().currentUser?.uid
        )

        viewModelScope.launch {
            try {
                db.collection("properties").add(property).addOnSuccessListener {
                    result = "Property added successfully"
                }.await().id
            } catch (e: Exception) {
                result = "An error occurred"
                e.message ?: "An error occurred"
            }
        }
        return result
    }
}
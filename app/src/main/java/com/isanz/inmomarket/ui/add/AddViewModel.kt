package com.isanz.inmomarket.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun save(
        tittle: String,
        description: String,
        location: String,
        images: List<String>,
        extras: HashMap<String, Int>,
        price: Double,
    ): String {
        var result = ""
        val property = Property(
            tittle = tittle,
            description = description,
            location = location,
            userId = InmoMarket.getAuth().currentUser?.uid ?: "",
            listImagesUri = images,
            extras = extras,
            price = price,

        )

        viewModelScope.launch {
            try {
                db.collection("properties").add(property).addOnSuccessListener {
                    result = "com.isanz.inmomarket.utils.entities.Property added successfully"
                }.await().id
            } catch (e: Exception) {
                result = "An error occurred"
                e.message ?: "An error occurred"
            }
        }
        return result
    }
}
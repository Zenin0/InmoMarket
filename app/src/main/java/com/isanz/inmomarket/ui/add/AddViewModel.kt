package com.isanz.inmomarket.ui.add

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.utils.Constants
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var property = MutableLiveData<Property>()

    fun setProperty(updatedProperty: Property) {
        property.value = updatedProperty
    }

    fun save(
        property: Property
    ) {

        viewModelScope.launch {
            try {
                db.collection("properties").add(property).addOnSuccessListener {}.await().id
            } catch (e: Exception) {
                Log.e(Constants.TAG, "save:failure", e)
            }
        }
    }
}

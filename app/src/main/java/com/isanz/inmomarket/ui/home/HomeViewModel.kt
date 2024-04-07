package com.isanz.inmomarket.ui.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _listParcelas = MutableLiveData<List<Property>>()
    val listParcelas: LiveData<List<Property>> = _listParcelas

    private val db = FirebaseFirestore.getInstance()

    init {
        viewModelScope.launch {
            try {
                listenForParcelasUpdates()
            } catch (e: Exception) {
                Log.w(TAG, "Listen failed.", e)
            }
        }
    }

    private suspend fun listenForParcelasUpdates() = withContext(Dispatchers.IO) {
        db.collection("properties").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val properties = mutableListOf<Property>()
                for (document in snapshot.documents) {
                    if (document.exists()) {
                        val property = document.toObject(Property::class.java)
                        property?.id = document.id
                        Log.i(TAG, "com.isanz.inmomarket.utils.entities.Property: $property")
                        property?.let { properties.add(it) }
                    }
                }
                _listParcelas.postValue(properties)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
}
package com.isanz.inmomarket.ui.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.isanz.inmomarket.InmoMarket
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
                logError(e)
            }
        }
    }

    private fun logError(e: Exception) {
        Log.w(TAG, "Listen failed.", e)
    }

    private suspend fun listenForParcelasUpdates() = withContext(Dispatchers.IO) {
        db.collection("properties").addSnapshotListener { snapshot, e ->
            if (e != null) {
                logError(e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                processSnapshot(snapshot)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun processSnapshot(snapshot: QuerySnapshot) {
        val properties = mutableListOf<Property>()
        val currentUserId = InmoMarket.getAuth().currentUser!!.uid
        for (document in snapshot.documents) {
            if (document.exists()) {
                val property = document.toObject(Property::class.java)
                property?.id = document.id
                if (property?.userId != currentUserId && property != null) {
                    properties.add(property)
                }
            }
        }
        _listParcelas.postValue(properties)
    }
}
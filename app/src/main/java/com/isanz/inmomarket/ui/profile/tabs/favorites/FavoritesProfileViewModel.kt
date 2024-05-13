package com.isanz.inmomarket.ui.profile.tabs.favorites

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesProfileViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    val db = InmoMarket.getDb()
    private val _listfavorites = MutableLiveData<MutableList<Property>>()
    val listFavorites: LiveData<MutableList<Property>> = _listfavorites

    init {
        viewModelScope.launch {
            try {
                listenForFavorites()
            } catch (e: Exception) {
                Log.w(ContentValues.TAG, "listenFavorites:failure.", e)
            }
        }
    }

    private suspend fun listenForFavorites() = withContext(dispatcher) {
        db.collection("properties").addSnapshotListener { snapshot, e ->
            if (e != null) {
                logFailure(e)
                return@addSnapshotListener
            }

            try {
                snapshot?.let {
                    handleSnapshot(it)
                } ?: logNullData()
            } catch (e: Exception) {
                logFailure(e)
            }
        }
    }

    private fun logFailure(e: Exception) {
        Log.w(ContentValues.TAG, "Listen failed.", e)
    }

    private fun logNullData() {
        Log.d(ContentValues.TAG, "Current data: null")
    }

    private fun handleSnapshot(snapshot: QuerySnapshot) {
        val properties = mutableListOf<Property>()
        val currentUserId = InmoMarket.getAuth().currentUser!!.uid
        for (document in snapshot.documents) {
            if (document.exists()) {
                handleDocument(document, currentUserId, properties)
            }
        }
        _listfavorites.postValue(properties)
    }

    private fun handleDocument(document: DocumentSnapshot, currentUserId: String, properties: MutableList<Property>) {
        val property = document.toObject(Property::class.java)
        property?.id = document.id
        if (property?.favorites!!.contains(currentUserId)) {
            property.let { properties.add(it) }
        }
    }
}
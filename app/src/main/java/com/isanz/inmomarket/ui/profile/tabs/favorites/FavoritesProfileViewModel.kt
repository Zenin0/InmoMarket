package com.isanz.inmomarket.ui.profile.tabs.favorites

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesProfileViewModel(userId: String = FirebaseAuth.getInstance().currentUser!!.uid , private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    val db = InmoMarket.getDb()
    private val _listfavorites = MutableLiveData<MutableList<Property>>()
    val listFavorites: LiveData<MutableList<Property>> = _listfavorites

    init {
        viewModelScope.launch {
            try {
                listenForFavorites(userId)
            } catch (e: Exception) {
                Log.w(ContentValues.TAG, "listenFavorites:failure.", e)
            }
        }
    }

    private suspend fun listenForFavorites(userId: String = FirebaseAuth.getInstance().currentUser!!.uid) = withContext(dispatcher) {
        db.collection("properties").addSnapshotListener { snapshot, e ->
            if (e != null) {
                logFailure(e)
                return@addSnapshotListener
            }

            try {
                snapshot?.let {
                    handleSnapshot(it, userId)
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

    private fun handleSnapshot(snapshot: QuerySnapshot, userId: String = InmoMarket.getAuth().currentUser!!.uid) {
        val properties = mutableListOf<Property>()
        for (document in snapshot.documents) {
            if (document.exists()) {
                handleDocument(document, userId, properties)
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

package com.isanz.inmomarket.ui.profile.tabs.favorites

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesProfileViewModel : ViewModel() {

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

    private suspend fun listenForFavorites() = withContext(Dispatchers.IO) {
        db.collection("properties").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            try {
                if (snapshot != null) {
                    val properties = mutableListOf<Property>()
                    val currentUserId = InmoMarket.getAuth().currentUser!!.uid
                    for (document in snapshot.documents) {
                        if (document.exists()) {
                            val property = document.toObject(Property::class.java)
                            property?.id = document.id
                            if (property?.favorites!!.contains(currentUserId)) {
                                property.let { properties.add(it) }
                            }
                        }
                    }
                    _listfavorites.postValue(properties)
                }
            } catch (e: Exception) {
                Log.w(ContentValues.TAG, "listenForFavorites:failure.", e)
            }
        }
    }
}
package com.isanz.inmomarket.ui.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.utils.entities.Parcela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _listParcelas = MutableLiveData<List<Parcela>>()
    val listParcelas: LiveData<List<Parcela>> = _listParcelas

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
        db.collection("propety").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val parcelas = snapshot.toObjects(Parcela::class.java)
                for (parcela in parcelas) {
                    parcela.id = snapshot.documents[parcelas.indexOf(parcela)].id
                }
                _listParcelas.postValue(parcelas)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
}
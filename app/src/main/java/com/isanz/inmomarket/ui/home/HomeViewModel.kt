package com.isanz.inmomarket.ui.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Parcela

class HomeViewModel : ViewModel() {

    private val _listParcelas = MutableLiveData<List<Parcela>>()
    val listParcelas: LiveData<List<Parcela>> = _listParcelas

    init {
        updateParcelas()
    }

    fun updateParcelas() {
        val db = InmoMarket.getDb()
        db.collection("parcelas").get().addOnSuccessListener { result ->
            val list = mutableListOf<Parcela>()
            for (document in result) {
                val parcela = document.toObject(Parcela::class.java)
                parcela.id = document.id
                list.add(parcela)
            }
            _listParcelas.value = list
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
    }
}

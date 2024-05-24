package com.isanz.inmomarket.ui.profile.tabs.uploads

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YourUploadsProfileViewModel(private val userId: String = InmoMarket.getAuth().currentUser!!.uid, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel()  {

    companion object {
        private const val TAG = "YourUploadsProfileVM"
    }

    private val _listParcelas = MutableLiveData<List<Property>>()
    val listParcelas: LiveData<List<Property>> = _listParcelas
    private val db = FirebaseFirestore.getInstance()

    init {
        viewModelScope.launch {
            try {
                listenForParcelasUpdates()
            } catch (e: Exception) {
                logFailure(e)
            }
        }
    }

    private suspend fun listenForParcelasUpdates()  {
        withContext(dispatcher){
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
    }

    private fun logFailure(e: Exception) {
        Log.w(TAG, "listenForParcelasUpdates:failure", e)
    }

    private fun logNullData() {
        Log.d(TAG, "Current data: null")
    }

    private fun handleSnapshot(snapshot: QuerySnapshot) {
        val properties = mutableListOf<Property>()
        for (document in snapshot.documents) {
            handleDocument(document, properties)
        }
        _listParcelas.postValue(properties)
    }

    private fun handleDocument(document: DocumentSnapshot, properties: MutableList<Property>) {
        document.toObject(Property::class.java)?.apply {
            id = document.id
            if (userId == this@YourUploadsProfileViewModel.userId) {
                properties.add(this)
            }
        }
    }
}

package com.isanz.inmomarket.ui.portal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PortalViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    private val _imageUrl = MutableLiveData<String>()
    fun getImageRandom() {
        viewModelScope.launch(dispatcher) {
            val url = try {
                val listResult =
                    InmoMarket.getStorage().reference.child("images/portal/").listAll().await()

                val images = listResult.items
                val image = images.random()
                val downloadUrl = image.downloadUrl.await()
                downloadUrl.toString()
            } catch (e: Exception) {
                Constants.LOGIN_IMAGE
            }
            _imageUrl.postValue(url)
        }
    }
}
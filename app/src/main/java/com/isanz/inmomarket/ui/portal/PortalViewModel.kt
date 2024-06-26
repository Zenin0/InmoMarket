package com.isanz.inmomarket.ui.portal

import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PortalViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    fun getImageRandom(): String {
        return runBlocking(dispatcher) {
            try {
                val listResult = InmoMarket.getStorage().reference.child("images/portal/").listAll().await()
                val images = listResult.items
                val image = images.random()
                val downloadUrl = image.downloadUrl.await()
                downloadUrl.toString()
            } catch (e: Exception) {
                Constants.LOGIN_IMAGE
            }
        }
    }
}

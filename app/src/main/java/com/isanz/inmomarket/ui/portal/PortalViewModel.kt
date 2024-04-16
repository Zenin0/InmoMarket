package com.isanz.inmomarket.ui.portal

import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class PortalViewModel : ViewModel() {

    suspend fun getImageRandom(): String {
        val url = try {
            val listResult = InmoMarket.getStorage().reference.child("images/portal/")
                .listAll()
                .await()

            val images = listResult.items
            val image = images.random()
            val downloadUrl = image.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Constants.LOGIN_IMAGE
        }
        return url
    }

}
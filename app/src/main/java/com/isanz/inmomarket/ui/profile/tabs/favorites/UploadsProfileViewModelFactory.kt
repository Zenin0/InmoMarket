package com.isanz.inmomarket.ui.profile.tabs.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.isanz.inmomarket.ui.profile.tabs.uploads.YourUploadsProfileViewModel

class UploadsProfileViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YourUploadsProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return YourUploadsProfileViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

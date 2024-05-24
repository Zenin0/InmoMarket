package com.isanz.inmomarket.ui.profile.tabs.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FavoritesProfileViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesProfileViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

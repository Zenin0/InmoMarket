package com.isanz.inmomarket.ui.profile

import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.User
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db = InmoMarket.getDb()

    suspend fun retrieveProfile(): User {
        val user =
            db.collection("users").document(InmoMarket.getAuth().currentUser!!.uid).get().await()
        return user.toObject(User::class.java)!!
    }



    fun signOut(): Boolean {
        return try {
            InmoMarket.getAuth().signOut()
            true
        } catch (e: Exception) {
            false
        }
    }


}
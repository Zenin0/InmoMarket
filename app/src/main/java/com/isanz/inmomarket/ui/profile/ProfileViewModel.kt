package com.isanz.inmomarket.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.User
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db = InmoMarket.getDb()

    suspend fun retrieveProfile(): User {
        val userId = InmoMarket.getAuth().currentUser!!.uid
        return getUserFromDb(userId)
    }

    private suspend fun getUserFromDb(userId: String): User {
        val user = db.collection("users").document(userId).get().await()
        return user.toObject(User::class.java)!!
    }

    fun updateUserProfilePhoto(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        uploadImageToStorage(userId, imageUri)
    }

    private fun uploadImageToStorage(userId: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().getReference("/images/$userId")
        storageRef.putFile(imageUri).addOnSuccessListener {
            updateProfilePhotoUrlInDb(userId, storageRef)
        }
    }

    private fun updateProfilePhotoUrlInDb(userId: String, storageRef: StorageReference) {
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).update("photoUrl", uri.toString())
        }
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
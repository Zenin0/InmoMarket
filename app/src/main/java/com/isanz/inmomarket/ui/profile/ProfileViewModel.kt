package com.isanz.inmomarket.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import com.isanz.inmomarket.utils.entities.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db = InmoMarket.getDb()

    fun retrieveProfile(profileUserId: String = FirebaseAuth.getInstance().currentUser!!.uid, callback: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = getUserFromDb(profileUserId)
                callback(user)
            } catch (e: Exception) {
                Log.e(Constants.TAG, "retrieveProfile:failure", e)
                callback(User())
            }
        }
    }

    private suspend fun getUserFromDb(userId: String): User {
        return try {
            val user = db.collection("users").document(userId).get().await()
            user.toObject(User::class.java)!!
        } catch (e: Exception) {
            Log.e(Constants.TAG, "getUserFromDb:failure", e)
            User()
        }
    }

    fun updateUserProfilePhoto(imageUri: Uri) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            uploadImageToStorage(userId, imageUri)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "updateUserProfilePhoto:failure", e)
        }
    }

    private fun uploadImageToStorage(userId: String, imageUri: Uri) {
        try {
            val storageRef = FirebaseStorage.getInstance().getReference("/images/$userId")
            storageRef.putFile(imageUri).addOnSuccessListener {
                updateProfilePhotoUrlInDb(userId, storageRef)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "uploadImageToStorage:failure", e)
        }
    }

    private fun updateProfilePhotoUrlInDb(userId: String, storageRef: StorageReference) {
        try {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId).update("photoUrl", uri.toString())
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "updateProfilePhotoUrlInDb:failure", e)
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

package com.isanz.inmomarket.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
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

    fun resetPassword() {
        InmoMarket.getAuth().sendPasswordResetEmail(InmoMarket.getAuth().currentUser!!.email!!)
    }

    fun changeProfilePhoto() {

    }


    fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit) {
        val userId = InmoMarket.getAuth().currentUser!!.uid
        val storageRef =
            FirebaseStorage.getInstance().reference.child("images/users/${userId}")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                db.collection("users").document(userId).update(
                    "photoUrl", userId
                )
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                // Handle failure
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

    fun closeAccount() {
        InmoMarket.getAuth().currentUser!!.delete()
        db.collection("users").document(InmoMarket.getAuth().currentUser!!.uid).delete()
    }
}
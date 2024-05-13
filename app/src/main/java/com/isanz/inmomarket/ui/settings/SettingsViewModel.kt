package com.isanz.inmomarket.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants

class SettingsViewModel : ViewModel() {

    private val db = InmoMarket.getDb()
    private val rd = Firebase.database

    fun closeAccount() {
        val userId = InmoMarket.getAuth().currentUser!!.uid
        deleteUserAccount(userId)
        deleteUserData(userId)
        deleteChats(userId)
    }

    private fun deleteUserAccount(userId: String) {
        try {
            InmoMarket.getAuth().currentUser!!.delete()
            db.collection("users").document(userId).delete()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "deleteUserAccount:failure", e)
        }
    }

    private fun deleteUserData(userId: String) {
        try {
            db.collection("properties").whereEqualTo("userId", userId).get().addOnSuccessListener {
                for (document in it.documents) {
                    document.reference.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(
                Constants.TAG, "deletUserData:failure", e
            )
        }
    }

    private fun deleteChats(userId: String) {
        try {
            val chatRef = rd.getReference("chats")
            chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (chatSnapshot in dataSnapshot.children) {
                        val membersId = chatSnapshot.child("membersId").value as List<*>
                        if (membersId.contains(userId)) {
                            chatSnapshot.ref.removeValue()
                            val chatId = chatSnapshot.key
                            val messageRef = rd.getReference("chatMessages/$chatId")
                            messageRef.removeValue()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(Constants.TAG, "deleteChats:onCancelled", databaseError.toException())
                }
            })
        } catch (e: Exception) {
            Log.e(Constants.TAG, "deleteChats:failure", e)
        }
    }
}
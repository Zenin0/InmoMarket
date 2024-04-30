package com.isanz.inmomarket.ui.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.InmoMarket

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
        InmoMarket.getAuth().currentUser!!.delete()
        db.collection("users").document(userId).delete()
    }

    private fun deleteUserData(userId: String) {
        db.collection("properties").whereEqualTo("userId", userId).get().addOnSuccessListener {
            for (document in it.documents) {
                document.reference.delete()
            }
        }
    }

    private fun deleteChats(userId: String) {
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
            }
        })
    }
}
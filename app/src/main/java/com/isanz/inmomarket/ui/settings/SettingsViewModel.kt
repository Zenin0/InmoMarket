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

        // Delete user from Firebase Authentication
        InmoMarket.getAuth().currentUser!!.delete()

        // Delete user from Firestore
        db.collection("users").document(userId).delete()
        db.collection("properties").whereEqualTo("userId", userId).get().addOnSuccessListener {
            for (document in it.documents) {
                document.reference.delete()
            }
        }

        // Delete all chats where the user is a member in Realtime Database
        val chatRef = rd.getReference("chats")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (chatSnapshot in dataSnapshot.children) {
                    val membersId = chatSnapshot.child("membersId").value as List<*>
                    if (membersId.contains(userId)) {
                        // Delete the chat
                        chatSnapshot.ref.removeValue()

                        // Delete all messages from the chat in Realtime Database
                        val chatId = chatSnapshot.key
                        val messageRef = rd.getReference("chatMessages/$chatId")
                        messageRef.removeValue()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
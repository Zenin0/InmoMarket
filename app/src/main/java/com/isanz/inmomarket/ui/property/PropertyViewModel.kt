package com.isanz.inmomarket.ui.property

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Chat
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class PropertyViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val database: FirebaseDatabase = Firebase.database
    private val user = InmoMarket.getAuth().currentUser

    fun getIfFavorite(property: Property, callback: (Boolean) -> Unit) {
        val docRef = db.collection("properties").document(property.id!!)
        docRef.get().addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? List<*>
            if (favorites != null && favorites.contains(user!!.uid)) {
                callback(true)
            } else {
                callback(false)
            }
        }
    }


    fun alterFavorite(property: Property, updateFavoriteIcon: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docRef = db.collection("properties").document(property.id!!)
            docRef.get().addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<*>
                if (favorites != null && favorites.contains(user!!.uid)) {
                    updateFavoriteIcon(false)
                    docRef.update("favorites", FieldValue.arrayRemove(user.uid))
                        .addOnFailureListener {
                            updateFavoriteIcon(true)
                        }
                } else {
                    updateFavoriteIcon(true)
                    docRef.update("favorites", FieldValue.arrayUnion(user!!.uid))
                        .addOnFailureListener {
                            updateFavoriteIcon(false)
                        }
                }
            }
        }
    }

    suspend fun retrieveProperty(propertyId: String): Property? {
        return try {
            val document = db.collection("properties").document(propertyId).get().await()
            if (document.exists()) {
                val property = document.toObject(Property::class.java)
                property?.id = document.id
                property
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(
                "com.isanz.inmomarket.ui.property.PropertyViewModel", "Error retrieving property", e
            )
            null
        }
    }

    fun createChat(senderId: String, recipientId: String, callback: (String) -> Unit) {
        val chatRef = database.getReference("chats")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var chatId: String? = null
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat?.membersId?.containsAll(
                            listOf(
                                senderId, recipientId
                            )
                        ) == true || chat?.membersId?.containsAll(
                            listOf(
                                recipientId, senderId
                            )
                        ) == true
                    ) {
                        chatId = snapshot.key
                        break
                    }
                }
                if (chatId == null) {
                    chatId = chatRef.push().key!!
                    chatRef.child(chatId).setValue(
                        Chat(
                            chatId = chatId, membersId = mutableListOf(senderId, recipientId)
                        )
                    )
                }
                callback(chatId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ChatViewModel", "Error adding chat", databaseError.toException())
            }
        })
    }

}
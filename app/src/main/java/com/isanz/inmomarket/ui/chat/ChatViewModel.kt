package com.isanz.inmomarket.ui.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.utils.entities.Message

class ChatViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database

    fun sendMessage(text: String, senderId: String, recipientId: String) {
        val message = Message(text = text, senderId = senderId, recipientId = recipientId)
        database.getReference("messages").push().setValue(message)
    }
}
package com.isanz.inmomarket.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.utils.entities.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, chatId: String, senderId: String) {
        val current = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val message = Message(
            message = text,
            senderId = senderId,
            messageDate = current.format(dateFormatter),
            messageTime = current.format(timeFormatter)
        )
        database.getReference("chatMessages").child(chatId).push().setValue(message)
    }
}
package com.isanz.inmomarket.ui.chat

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.utils.entities.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel : ViewModel() {
    private val database: FirebaseDatabase = Firebase.database
    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>> get() = _messageList

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

    fun retrieveMessages(chatId: String) {
        val messageRef = database.getReference("chatMessages").child(chatId)
        val messageListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tempList = mutableListOf<Message>()
                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        tempList.add(message)
                    }
                }
                _messageList.value = tempList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadMessage:onCancelled", databaseError.toException())
            }
        }
        messageRef.addValueEventListener(messageListener)
    }
}
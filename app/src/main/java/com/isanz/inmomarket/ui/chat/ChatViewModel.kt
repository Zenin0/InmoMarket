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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.utils.entities.Conversation
import com.isanz.inmomarket.utils.entities.Message
import com.isanz.inmomarket.utils.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatViewModel : ViewModel() {

    private val database: FirebaseDatabase = Firebase.database
    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>> get() = _messageList

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String, chatId: String, senderId: String) {
        val message = createMessage(text, senderId)
        try {
            database.getReference("chatMessages").child(chatId).push().setValue(message)
        } catch (e: Exception) {
            Log.e(TAG, "saveMessageToChatMessage:failure", e)
        }
        try {
            database.getReference("chats").child(chatId).child("lastMessage").setValue(message)
        } catch (e: Exception) {
            Log.e(TAG, "saveMessageToChats:failure", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMessage(text: String, senderId: String): Message {
        val current = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return Message(
            message = text,
            senderId = senderId,
            messageDate = current.format(dateFormatter),
            messageTime = current.format(timeFormatter)
        )
    }

    fun getUsersInConversation(chatId: String): Deferred<List<User>> {
        return CoroutineScope(Dispatchers.IO).async {
            try {
                val chatSnapshot = getChatSnapshot(chatId)
                val chat = chatSnapshot.getValue(Conversation::class.java)
                getUsersFromChat(chat)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting users in conversation", e)
                emptyList()
            }
        }
    }

    private suspend fun getChatSnapshot(chatId: String) = suspendCoroutine { continuation ->
        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    private suspend fun getUsersFromChat(chat: Conversation?): List<User> {
        val users = emptyList<User>().toMutableList()
        for (memberId in chat?.membersId ?: emptyList()) {
            try {
                val userRef = Firebase.firestore.collection("users").document(memberId)
                val userSnapshot = userRef.get().await()
                val user = userSnapshot.toObject(User::class.java)
                if (user != null) {
                    user.uid = userSnapshot.id
                    users.add(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getUsersFromChat:failure", e)
            }
        }
        return users.toList()
    }

    fun retrieveMessages(chatId: String) {
        val messageRef = database.getReference("chatMessages").child(chatId)
        val messageListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                updateMessageList(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadMessage:onCancelled", databaseError.toException())
            }
        }
        messageRef.addValueEventListener(messageListener)
    }

    private fun updateMessageList(dataSnapshot: DataSnapshot) {
        val tempList = mutableListOf<Message>()
        for (messageSnapshot in dataSnapshot.children) {
            val message = messageSnapshot.getValue(Message::class.java)
            if (message != null) {
                tempList.add(message)
            }
        }
        _messageList.value = tempList
    }
}
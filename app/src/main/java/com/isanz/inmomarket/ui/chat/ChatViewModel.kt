package com.isanz.inmomarket.ui.chat

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    private val database: FirebaseDatabase = Firebase.database
    private val _messageList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>> get() = _messageList

    private val _messageSentStatus = MutableLiveData<Boolean>()

    fun sendMessage(text: String, chatId: String, senderId: String) {
        viewModelScope.launch(dispatcher) {
            val message = createMessage(text, senderId)
            try {
                database.getReference("chatMessages").child(chatId).push().setValue(message).await()
                database.getReference("chats").child(chatId).child("lastMessage").setValue(message)
                    .await()
                _messageSentStatus.postValue(true)
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage:failure", e)
                _messageSentStatus.postValue(false)
            }
        }
    }


    private fun createMessage(text: String, senderId: String): Message {
        val current = Calendar.getInstance().time
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return Message(
            message = text,
            senderId = senderId,
            messageDate = dateFormatter.format(current),
            messageTime = timeFormatter.format(current)
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

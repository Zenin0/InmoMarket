package com.isanz.inmomarket.ui.conversations

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.Constants
import com.isanz.inmomarket.utils.entities.Conversation

class ConversationsViewModel : ViewModel() {


    val listConversations get() = _listConversations

    private val _listConversations = MutableLiveData<List<Conversation>>()

    private val database: FirebaseDatabase = Firebase.database

    private val userId = InmoMarket.getAuth().currentUser!!.uid

    @RequiresApi(Build.VERSION_CODES.O)
    fun sortConversations(conversations: List<Conversation>): List<Conversation> {
        return conversations.sortedByDescending {
            if (it.lastMessage.messageDate.isNotBlank() && it.lastMessage.messageTime.isNotBlank()) {
                val dateTimeStr = "${it.lastMessage.messageDate} ${it.lastMessage.messageTime}"
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                LocalDateTime.parse(dateTimeStr, formatter)
            } else {
                LocalDateTime.MIN
            }
        }
    }

    fun retrieveConversationFromSnapshot(snapshot: com.google.firebase.database.DataSnapshot): MutableList<Conversation> {
        val conversations = mutableListOf<Conversation>()
        for (chatSnapshot in snapshot.children) {
            val conversation = chatSnapshot.getValue(Conversation::class.java)
            if (conversation != null && conversation.membersId.contains(userId)) {
                conversations.add(conversation)
            }
        }
        return conversations
    }

    fun retrieveConversations() {
        try {
        val chatsRef = database.getReference("chats")
        val messageListener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val conversations = retrieveConversationFromSnapshot(snapshot)
                val sortedConversations = sortConversations(conversations)
                _listConversations.value = sortedConversations
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.w(Constants.TAG, "retrieveConversations:onCancelled", error.toException())
            }
        }
        chatsRef.addValueEventListener(messageListener)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "retrieveConversations:failure", e)
        }
    }
}
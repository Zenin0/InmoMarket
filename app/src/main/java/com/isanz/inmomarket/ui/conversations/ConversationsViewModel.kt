package com.isanz.inmomarket.ui.conversations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Conversation

class ConversationsViewModel : ViewModel() {


    val listConversations get() = _listConversations

    private val _listConversations = MutableLiveData<List<Conversation>>()

    private val database: FirebaseDatabase = Firebase.database

    private val userId = InmoMarket.getAuth().currentUser!!.uid

    fun retrieveConversations() {
        val chatsRef = database.getReference("chats")
        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val conversations = mutableListOf<Conversation>()
                for (chatSnapshot in snapshot.children) {
                    val conversation = chatSnapshot.getValue(Conversation::class.java)
                    if (conversation != null && conversation.membersId.contains(userId)) {
                        conversations.add(conversation)
                    }
                }
                _listConversations.value = conversations
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {

            }
        }
        chatsRef.addValueEventListener(messageListener)
    }
}
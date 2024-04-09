package com.isanz.inmomarket.ui.conversations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.Conversation
import com.isanz.inmomarket.utils.entities.User

class ConversationsViewModel : ViewModel() {


    val listConversations get() = _listConversations

    private val _listConversations = MutableLiveData<List<Conversation>>()

    private val database: FirebaseDatabase = Firebase.database

    private val userId = InmoMarket.getAuth().currentUser!!.uid

    private val _usersLiveData = MutableLiveData<List<User>>()
    val usersLiveData: LiveData<List<User>> = _usersLiveData

    fun getUsersInConversation(idUsuarios: List<String>) {
        val users = mutableListOf<User>()
        for (id in idUsuarios) {
            InmoMarket.getDb().collection("users").document(id).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.toObject(User::class.java)
                        user?.let {
                            users.add(user)
                            _usersLiveData.value = users  // Notify observers with updated list
                        }
                    } else {
                        Log.e("ConversationViewModel", "Error getting user: ", task.exception)
                    }
                }
        }
    }

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
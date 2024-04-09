package com.isanz.inmomarket.ui.rv.conversationItem

import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.User

class ConversationViewModel : ViewModel() {


    fun getUsersInConversation(idUsuarios: List<String>, callback: (List<User>) -> Unit) {
        val users = mutableListOf<User>()
        for (id in idUsuarios) {
            InmoMarket.getDb().collection("users").document(id).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result.toObject(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                callback(users)
            }
        }
    }

}
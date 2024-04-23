package com.isanz.inmomarket.rv.conversationItem

import androidx.lifecycle.ViewModel
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.utils.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await

class ConversationViewModel : ViewModel() {

    suspend fun getUsersInConversation(idUsuarios: List<String>): List<User> {
        val deferreds = idUsuarios.map { id ->
            CoroutineScope(Dispatchers.IO).async {
                val document = InmoMarket.getDb().collection("users").document(id).get().await()
                document.toObject(User::class.java)?.also { user ->
                    user.uid = id
                } ?: run {
                    null
                }
            }
        }

        return deferreds.awaitAll().filterNotNull()
    }

}
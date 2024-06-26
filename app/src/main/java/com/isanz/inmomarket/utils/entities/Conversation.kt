package com.isanz.inmomarket.utils.entities

import com.google.firebase.database.Exclude

data class Conversation(
    @Exclude val chatId: String = "",
    val lastMessage: Message = Message(),
    val membersId: MutableList<String> = mutableListOf(),
)

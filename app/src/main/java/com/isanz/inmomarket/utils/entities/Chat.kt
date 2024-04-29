package com.isanz.inmomarket.utils.entities

import com.google.firebase.database.Exclude

data class Chat(
    @Exclude val chatId: String = "",
    val membersId: MutableList<String> = mutableListOf<String>(),
    val lastMessage: Message? = null
)

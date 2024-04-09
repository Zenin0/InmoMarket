package com.isanz.inmomarket.utils.entities

data class Conversation(
    val chatId: String = "",
    val lastMessage: String = "",
    val membersId: MutableList<String> = mutableListOf(),
)

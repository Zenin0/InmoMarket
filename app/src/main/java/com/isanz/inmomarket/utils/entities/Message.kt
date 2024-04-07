package com.isanz.inmomarket.utils.entities

data class Message(
    val id : String = "",
    val text : String = "",
    val senderId : String = "",
    val recipientId : String = "",
    val timestamp : Long = 0
)

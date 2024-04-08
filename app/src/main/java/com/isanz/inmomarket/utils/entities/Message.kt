package com.isanz.inmomarket.utils.entities

import com.google.firebase.firestore.Exclude


data class Message(
    @Exclude val messageId : String = "",
    val message : String = "",
    val senderId : String = "",
    val messageDate : String = "",
    val messageTime : String = ""

)

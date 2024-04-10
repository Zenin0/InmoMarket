package com.isanz.inmomarket.rv.chatItem

import androidx.recyclerview.widget.DiffUtil
import com.isanz.inmomarket.utils.entities.Message

class ChatDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.message == newItem.message && oldItem.senderId == newItem.senderId && oldItem.messageDate == newItem.messageDate && oldItem.messageTime == newItem.messageTime
    }
}
package com.isanz.inmomarket.rv.conversationItem

import androidx.recyclerview.widget.DiffUtil
import com.isanz.inmomarket.utils.entities.Conversation

class ChatDiffCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.chatId == newItem.chatId
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }
}
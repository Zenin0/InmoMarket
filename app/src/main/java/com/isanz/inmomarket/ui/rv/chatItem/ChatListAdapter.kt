package com.isanz.inmomarket.ui.rv.chatItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Message

class ChatListAdapter : ListAdapter<Message, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val text: TextView = itemView.findViewById(R.id.tvTextMessage)
        val time: TextView = itemView.findViewById(R.id.tvMessageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        holder.text.text = message.message
        holder.time.text = message.messageTime
    }


}
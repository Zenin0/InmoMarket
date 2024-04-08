package com.isanz.inmomarket.ui.rv.chatItem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Message

class ChatListAdapter : ListAdapter<Message, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val selfId = InmoMarket.getAuth().currentUser!!.uid

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvTextMessage)
        val time: TextView = itemView.findViewById(R.id.tvMessageTime)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == selfId) {
            R.layout.chat_item_m
        } else {
            R.layout.chat_item_o
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        holder.text.text = message.message
        val parts = message.messageTime.split(":")
        val part1 = parts[0]
        val part2 = parts[1]
        "$part1:$part2".also { holder.time.text = it }
    }
}
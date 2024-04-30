package com.isanz.inmomarket.rv.chatItem

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Message
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChatListAdapter : ListAdapter<Message, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val selfId = InmoMarket.getAuth().currentUser!!.uid
    private var lastDate: LocalDate? = null

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvTextMessage)
        val time: TextView = itemView.findViewById(R.id.tvMessageTime)
        val cvDate: CardView = itemView.findViewById(R.id.cvDate)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        holder.text.text = message.message
        val parts = message.messageTime.split(":")
        val part1 = parts[0]
        val part2 = parts[1]
        "$part1:$part2".also { holder.time.text = it }

        val messageDate = LocalDate.parse(message.messageDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (position == 0 || messageDate != lastDate) {
            holder.cvDate.visibility = View.VISIBLE
            holder.tvDate.text = messageDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            lastDate = messageDate
        } else {
            holder.cvDate.visibility = View.GONE
        }
    }
}
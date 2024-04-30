package com.isanz.inmomarket.rv.conversationItem

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Conversation
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConversationListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Conversation, ConversationListAdapter.ConversationViewHolder>(ChatDiffCallback()) {

    private val viewModel = ConversationViewModel()


    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.profile_image)
        val name: TextView = itemView.findViewById(R.id.username)
        val lastMessage: TextView = itemView.findViewById(R.id.message)
        val time: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)
        CoroutineScope(Dispatchers.Main).launch {
            val users = viewModel.getUsersInConversation(conversation.membersId)
            val myId = InmoMarket.getAuth().currentUser?.uid
            val otherUser = users.find { it.uid != myId }
            if (otherUser != null) {
                if (otherUser.displayName == null) {
                    holder.name.text = otherUser.email
                } else {
                    holder.name.text = otherUser.displayName
                }
                if (otherUser.photoUrl != null) {
                    setImage(holder.image, otherUser.photoUrl)
                }
            }
        }
        holder.lastMessage.text = conversation.lastMessage.message

        if (conversation.lastMessage.messageDate.isNotBlank() && conversation.lastMessage.messageTime.isNotBlank()) {
            val messageDateTimeStr = "${conversation.lastMessage.messageDate} ${conversation.lastMessage.messageTime}"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val messageDateTime = LocalDateTime.parse(messageDateTimeStr, formatter)
            val now = LocalDateTime.now()
            val displayDateTime = if (messageDateTime.toLocalDate().isBefore(now.toLocalDate())) {
                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM")
                messageDateTime.toLocalDate().format(dateFormatter)
            } else {
                conversation.lastMessage.messageTime.substring(0, 5)
            }

            holder.time.text = displayDateTime
        }

        holder.itemView.setOnClickListener {
            this.listener.onItemClicked(conversation.chatId)
        }
    }


    private fun setImage(view: ImageView, uri: String) {
        Glide.with(view.context).load(uri).circleCrop().into(view)
    }
}
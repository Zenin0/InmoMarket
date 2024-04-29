package com.isanz.inmomarket.rv.conversationItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.utils.entities.Conversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationListAdapter(private val navController: NavController) :
    ListAdapter<Conversation, ConversationListAdapter.ConversationViewHolder>(ChatDiffCallback()) {

    private val viewModel = ConversationViewModel()


    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.profile_image)
        val name: TextView = itemView.findViewById(R.id.username)
        val lastMessage: TextView = itemView.findViewById(R.id.message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)

        holder.name.text = ""
        holder.image.setImageDrawable(null)

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
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("idChat", conversation.chatId)
            }
            navController.navigate(R.id.action_navigation_messages_to_navigation_chat, bundle)
        }
    }


    private fun setImage(view: ImageView, uri: String) {
        Glide.with(view.context).load(uri).circleCrop().into(view)
    }
}
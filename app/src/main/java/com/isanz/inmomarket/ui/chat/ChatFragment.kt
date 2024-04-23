package com.isanz.inmomarket.ui.chat

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentChatBinding
import com.isanz.inmomarket.rv.chatItem.ChatListAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ChatFragment : Fragment() {


    private lateinit var mBinding: FragmentChatBinding

    private lateinit var viewModel: ChatViewModel

    private lateinit var idChat: String

    private lateinit var recipientId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idChat = requireArguments().getString("idChat")!!
        recipientId = InmoMarket.getAuth().currentUser?.uid!!
        viewModel = ChatViewModel()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentChatBinding.inflate(inflater, container, false)
        setUpView()

        return mBinding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpView() {
        lifecycleScope.launch {
            val users = viewModel.getUsersInConversation(idChat).await()
            val otherUser = users.find { it.uid != recipientId }
            mBinding.tvNameChat.text = otherUser?.displayName
            otherUser?.photoUrl?.let { loadImage(it) }
        }
        setUpButtons()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val adapter = ChatListAdapter()
        val layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = adapter
        viewModel.messageList.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                // Scroll to the last position after the list has been updated
                mBinding.recyclerView.scrollToPosition(0)
            }
        }
        viewModel.retrieveMessages(idChat)
    }

    private fun loadImage(imageUri: String){
        Glide.with(this)
            .load(imageUri)
            .circleCrop()
            .into(mBinding.ivProfileChat)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpButtons() {
        mBinding.fabSendMessage.setOnClickListener {
            val text = mBinding.tieMessage.text.toString()
            viewModel.sendMessage(text, idChat, recipientId)
            mBinding.tieMessage.text?.clear()
        }
        mBinding.ibBack.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_chat_to_navigation_messages)
        }
    }
}
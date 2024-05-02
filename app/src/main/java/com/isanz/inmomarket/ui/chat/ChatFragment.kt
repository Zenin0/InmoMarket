package com.isanz.inmomarket.ui.chat

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.databinding.FragmentChatBinding
import com.isanz.inmomarket.rv.chatItem.ChatListAdapter
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {


    private lateinit var mBinding: FragmentChatBinding
    private lateinit var idChat: String
    private lateinit var recipientId: String
    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idChat = requireArguments().getString("idChat")!!
        recipientId = InmoMarket.getAuth().currentUser?.uid!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentChatBinding.inflate(inflater, container, false)
        setUpView()
        return mBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpView() {
        setUpOtherUser()
        setUpButtons()
        setUpRecyclerView()
    }

    private fun setUpOtherUser() = lifecycleScope.launch {
        val users = chatViewModel.getUsersInConversation(idChat).await()
        val otherUser = users.find { it.uid != InmoMarket.getAuth().currentUser!!.uid }
        mBinding.tvNameChat.text = otherUser?.displayName
        otherUser?.photoUrl?.let { loadImage(it) }
    }

    private fun setUpRecyclerView() {
        val adapter = ChatListAdapter()
        val layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = adapter
        observeMessages(adapter)
        chatViewModel.retrieveMessages(idChat)
    }

    private fun observeMessages(adapter: ChatListAdapter) {
        chatViewModel.messageList.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                val lastPosition = messages.size - 1
                if (lastPosition >= 0) {
                    mBinding.recyclerView.scrollToPosition(lastPosition)
                }
            }
        }
    }

    private fun loadImage(imageUri: String) {
        Glide.with(this).load(imageUri).circleCrop().into(mBinding.ivProfileChat)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpButtons() {
        mBinding.fabSendMessage.setOnClickListener {
            sendMessage()
        }
        mBinding.ibBack.setOnClickListener {
            navigateBack()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage() {
        val text = mBinding.tieMessage.text.toString()
        chatViewModel.sendMessage(text, idChat, recipientId)
        mBinding.tieMessage.text?.clear()
    }

    private fun navigateBack() {
        this.findNavController().popBackStack()
    }
}
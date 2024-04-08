package com.isanz.inmomarket.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.databinding.FragmentChatBinding
import com.isanz.inmomarket.ui.rv.chatItem.ChatListAdapter

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentChatBinding.inflate(inflater, container, false)
        setUpButtons()
        setUpRecyclerView()
        return mBinding.root

    }

    private fun setUpRecyclerView() {
        val adapter = ChatListAdapter()
        mBinding.recyclerView.adapter = adapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.messageList.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
        }
        viewModel.retrieveMessages(idChat)
    }


    private fun setUpButtons() {
        mBinding.fabSendMessage.setOnClickListener {
            val text = mBinding.tieMessage.text.toString()
            viewModel.sendMessage(text, idChat, recipientId)
            mBinding.tieMessage.text?.clear()
        }
    }
}
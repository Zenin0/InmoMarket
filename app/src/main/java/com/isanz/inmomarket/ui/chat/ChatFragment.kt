package com.isanz.inmomarket.ui.chat

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.databinding.FragmentChatBinding

class ChatFragment : Fragment() {


    private lateinit var mBinding: FragmentChatBinding

    private lateinit var viewModel: ChatViewModel

    private var senderId: String? = null

    private var recipientId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        senderId = arguments?.getString("idUser")
        recipientId = InmoMarket.getAuth().currentUser?.uid
        viewModel = ChatViewModel()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentChatBinding.inflate(inflater, container, false)
        setUpButtons()
        return mBinding.root

    }

    private fun setUpButtons() {
        mBinding.fabSendMessage.setOnClickListener {
            val text = mBinding.tieMessage.text.toString()
            if (senderId != null && recipientId != null) {
                viewModel.sendMessage(text, senderId!!, recipientId!!)
                mBinding.tieMessage.text?.clear()
            } else {
                Log.i(TAG, "Error: $senderId, $recipientId")
            }
        }
    }
}
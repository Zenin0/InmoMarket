package com.isanz.inmomarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.isanz.inmomarket.databinding.FragmentChatBinding

class ChatFragment : Fragment() {


    private lateinit var mBinding: FragmentChatBinding

    private lateinit var viewModel: ChatViewModel

    private var userId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("propertyId")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
}
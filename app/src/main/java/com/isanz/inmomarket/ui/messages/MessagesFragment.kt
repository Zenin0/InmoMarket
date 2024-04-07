package com.isanz.inmomarket.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isanz.inmomarket.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private lateinit var mBinding: FragmentMessagesBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[MessagesViewModel::class.java]
        mBinding = FragmentMessagesBinding.inflate(inflater, container, false)
        return mBinding.root
    }


}
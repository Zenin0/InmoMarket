package com.isanz.inmomarket.ui.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.databinding.FragmentConversationsBinding
import com.isanz.inmomarket.rv.conversationItem.ConversationListAdapter

class ConversationsFragment : Fragment() {

    private lateinit var mBinding: FragmentConversationsBinding
    private val viewModel: ConversationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentConversationsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val adapter = ConversationListAdapter(findNavController())
        val layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = adapter

        viewModel.listConversations.observe(viewLifecycleOwner) { conversations ->
            adapter.submitList(conversations) {
                // Scroll to the last position after the list has been updated
                mBinding.recyclerView.scrollToPosition(0)
            }
            if (conversations.isEmpty()) {
                mBinding.tvNoConversations.visibility = View.VISIBLE
            } else {
                mBinding.tvNoConversations .visibility = View.GONE
            }
            mBinding.progressBar.visibility = View.GONE
        }

        viewModel.retrieveConversations()
    }
}

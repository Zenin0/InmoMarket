package com.isanz.inmomarket.ui.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentConversationsBinding
import com.isanz.inmomarket.rv.conversationItem.ConversationListAdapter
import com.isanz.inmomarket.utils.entities.Conversation
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener

class ConversationsFragment : Fragment(), OnItemClickListener {

    private lateinit var mBinding: FragmentConversationsBinding
    private val conversationsViewModel: ConversationsViewModel by lazy {
        ViewModelProvider(this)[ConversationsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentConversationsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        conversationsViewModel.retrieveConversations()
    }

    private fun setUpRecyclerView() {
        val adapter = ConversationListAdapter(this)
        setupLayoutManager()
        mBinding.recyclerView.adapter = adapter
        observeConversations(adapter)
    }

    private fun setupLayoutManager() {
        val layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.layoutManager = layoutManager
    }

    private fun observeConversations(adapter: ConversationListAdapter) {
        conversationsViewModel.listConversations.observe(viewLifecycleOwner) { conversations ->
            updateRecyclerView(conversations, adapter)
        }
    }

    private fun updateRecyclerView(
        conversations: List<Conversation>,
        adapter: ConversationListAdapter
    ) {
        adapter.submitList(conversations) {
            mBinding.recyclerView.scrollToPosition(0)
        }
        updateVisibilityBasedOnContent(conversations)
    }

    private fun updateVisibilityBasedOnContent(conversations: List<Conversation>) {
        if (conversations.isEmpty()) {
            mBinding.emptyTextView.visibility = View.VISIBLE
        } else {
            mBinding.emptyTextView.visibility = View.GONE
        }
        mBinding.progressBar.visibility = View.GONE
    }

    override fun onItemClicked(propertyId: String) {
        val bundle = Bundle().apply {
            putString("idChat", propertyId)
        }
        findNavController().navigate(R.id.action_navigation_messages_to_navigation_chat, bundle)
    }
}

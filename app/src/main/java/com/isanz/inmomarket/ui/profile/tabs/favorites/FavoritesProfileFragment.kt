package com.isanz.inmomarket.ui.profile.tabs.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentFavoritesProfileBinding
import com.isanz.inmomarket.rv.propertyItem.PropertyItemListAdapter
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener


class FavoritesProfileFragment : Fragment(), OnItemClickListener {


    private lateinit var mBinging: FragmentFavoritesProfileBinding
    private lateinit var favoritesProfileViewModel: FavoritesProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinging = FragmentFavoritesProfileBinding.inflate(inflater, container, false)
        favoritesProfileViewModel = FavoritesProfileViewModel()
        setupRecyclerView()
        return mBinging.root
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    override fun onItemClicked(propertyId: String) {
        val bundle = Bundle().apply {
            putString("propertyId", propertyId)
        }
        this.findNavController()
            .navigate(R.id.action_navigation_profile_to_navigation_property, bundle)
    }


    private fun setupRecyclerView() {
        val adapter = PropertyItemListAdapter(this)
        mBinging.favoritesRecyclerView.adapter = adapter
        mBinging.favoritesRecyclerView.layoutManager = LinearLayoutManager(context)
        favoritesProfileViewModel.listFavorites.observe(viewLifecycleOwner) { parcelas ->
            adapter.submitList(parcelas)
            if (parcelas.isEmpty()) {
                mBinging.emptyTextView.visibility = View.VISIBLE
            } else {
                mBinging.emptyTextView.visibility = View.GONE
            }
            mBinging.favoritesProgressBar.visibility = View.GONE
        }
    }

}
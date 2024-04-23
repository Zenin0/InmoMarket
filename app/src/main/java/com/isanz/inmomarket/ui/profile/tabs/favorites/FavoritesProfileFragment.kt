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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinging = FragmentFavoritesProfileBinding.inflate(inflater, container, false)
        favoritesProfileViewModel = FavoritesProfileViewModel()
        setupRecyclerView()
        return mBinging.root
    }

    override fun onItemClicked(propertyId: String) {
        val bundle = Bundle().apply {
            putString("propertyId", propertyId)
        }
        this.findNavController().navigate(R.id.action_navigation_profile_to_navigation_property, bundle)
    }


    private fun setupRecyclerView() {
        val adapter = PropertyItemListAdapter(this)
        mBinging.favoritesRecyclerView.adapter = adapter

        // Set the LayoutManager
        mBinging.favoritesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe the data from the ViewModel
        favoritesProfileViewModel.listFavorites.observe(viewLifecycleOwner) { parcelas ->
            // Update the adapter with the new list of Parcela objects
            adapter.submitList(parcelas)
            if (parcelas.isEmpty()) {
                mBinging.favoritesEmptyTextView.visibility = View.VISIBLE
            } else {
                mBinging.favoritesEmptyTextView.visibility = View.GONE
            }
            mBinging.favoritesProgressBar.visibility = View.GONE
        }
    }

}
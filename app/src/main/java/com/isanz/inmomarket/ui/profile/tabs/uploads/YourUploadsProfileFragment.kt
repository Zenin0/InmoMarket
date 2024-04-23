package com.isanz.inmomarket.ui.profile.tabs.uploads

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentFavoritesProfileBinding
import com.isanz.inmomarket.databinding.FragmentYourUploadsProfileBinding
import com.isanz.inmomarket.rv.propertyItem.PropertyItemListAdapter
import com.isanz.inmomarket.ui.profile.tabs.favorites.FavoritesProfileViewModel
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener


class YourUploadsProfileFragment : Fragment(), OnItemClickListener {


    private lateinit var mBinging: FragmentYourUploadsProfileBinding

    private lateinit var favoritesProfileViewModel: YourUploadsProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinging = FragmentYourUploadsProfileBinding.inflate(inflater, container, false)
        favoritesProfileViewModel = YourUploadsProfileViewModel()
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
        this.findNavController().navigate(R.id.action_navigation_profile_to_navigation_property, bundle)
    }


    private fun setupRecyclerView() {
        val adapter = PropertyItemListAdapter(this)
        mBinging.favoritesRecyclerView.adapter = adapter
        adapter.attachToRecyclerView(mBinging.favoritesRecyclerView)

        // Set the LayoutManager
        mBinging.favoritesRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe the data from the ViewModel
        favoritesProfileViewModel.listParcelas.observe(viewLifecycleOwner) { parcelas ->
            // Update the adapter with the new list of Parcela objects
            adapter.submitList(parcelas)
            Log.i("FavoritesProfileFragment", "Parcelas: $parcelas")
            if (parcelas.isEmpty()) {
                mBinging.emptyTextView.visibility = View.VISIBLE
            } else {
                mBinging.emptyTextView.visibility = View.GONE
            }
            mBinging.favoritesProgressBar.visibility = View.GONE
        }
    }

}
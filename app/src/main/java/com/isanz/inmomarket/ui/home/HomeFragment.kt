package com.isanz.inmomarket.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentHomeBinding
import com.isanz.inmomarket.rv.propertyItem.PropertyItemListAdapter
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener

class HomeFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setUpView()
        return binding.root
    }

    private fun setUpView() {
        MobileAds.initialize(requireContext()) {}

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        setupRecyclerView(homeViewModel)
    }

    private fun setupRecyclerView(homeViewModel: HomeViewModel) {
        // Initialize the RecyclerView adapter
        val adapter = PropertyItemListAdapter(this)
        binding.rvHome.adapter = adapter

        // Set the LayoutManager
        binding.rvHome.layoutManager = LinearLayoutManager(context)

        // Observe the data from the ViewModel
        homeViewModel.listParcelas.observe(viewLifecycleOwner) { parcelas ->
            // Update the adapter with the new list of Parcela objects
            adapter.submitList(parcelas)
            if (parcelas.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
            } else {
                binding.emptyTextView.visibility = View.GONE
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onItemClicked(propertyId: String) {
        val bundle = Bundle().apply {
            putString("propertyId", propertyId)
        }
        this.findNavController().navigate(R.id.action_navigation_home_to_propertyFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
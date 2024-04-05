package com.isanz.inmomarket.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isanz.inmomarket.databinding.FragmentHomeBinding
import com.isanz.inmomarket.ui.rv.parcelaItem.ParcelaListAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerView(homeViewModel)
        return binding.root
    }

    private fun setupRecyclerView(homeViewModel: HomeViewModel) {
        // Initialize the RecyclerView adapter
        val adapter = ParcelaListAdapter()
        binding.rvHome.adapter = adapter

        // Set the LayoutManager
        binding.rvHome.layoutManager = LinearLayoutManager(context)

        // Observe the data from the ViewModel
        homeViewModel.listParcelas.observe(viewLifecycleOwner) { parcelas ->
            // Update the adapter with the new list of Parcela objects
            adapter.submitList(parcelas)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
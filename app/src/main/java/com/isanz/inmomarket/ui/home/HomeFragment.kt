package com.isanz.inmomarket.ui.home

import android.content.Context
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
import com.isanz.inmomarket.ui.dialog.SettingsDialogFragment
import com.isanz.inmomarket.utils.entities.Property
import com.isanz.inmomarket.utils.interfaces.OnItemClickListener

class HomeFragment : Fragment(), OnItemClickListener {

    private lateinit var mBinding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        setUpView()
        return mBinding.root
    }

    private fun setUpView() {
        initializeMobileAds()
        loadAdRequest()
        checkAndShowBiometricLoginPopUp()
        setupRecyclerView(homeViewModel)
    }

    private fun initializeMobileAds() {
        MobileAds.initialize(requireContext()) {}
    }

    private fun loadAdRequest() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }

    private fun checkAndShowBiometricLoginPopUp() {
        val sharedPref =
            requireContext().getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("biometricLoginPopUp", true)) {
            SettingsDialogFragment().show(childFragmentManager, "SettingsDialogFragment")
            sharedPref.edit().putBoolean("biometricLoginPopUp", false).apply()
        }
    }

    private fun setupRecyclerView(homeViewModel: HomeViewModel) {
        val adapter = PropertyItemListAdapter(this)
        mBinding.rvHome.adapter = adapter
        mBinding.rvHome.layoutManager = LinearLayoutManager(context)
        observeParcelas(homeViewModel, adapter)
    }

    private fun observeParcelas(homeViewModel: HomeViewModel, adapter: PropertyItemListAdapter) {
        homeViewModel.listParcelas.observe(viewLifecycleOwner) { parcelas ->
            updateRecyclerView(parcelas, adapter)
        }
    }

    private fun updateRecyclerView(parcelas: List<Property>, adapter: PropertyItemListAdapter) {
        adapter.submitList(parcelas)
        if (parcelas.isEmpty()) {
            mBinding.emptyTextView.visibility = View.VISIBLE
        } else {
            mBinding.emptyTextView.visibility = View.GONE
        }
        mBinding.progressBar.visibility = View.GONE
    }

    override fun onItemClicked(propertyId: String) {
        navigateToProperty(propertyId)
    }

    private fun navigateToProperty(propertyId: String) {
        val bundle = Bundle().apply {
            putString("propertyId", propertyId)
        }
        this.findNavController().navigate(R.id.action_navigation_home_to_propertyFragment, bundle)
    }
}
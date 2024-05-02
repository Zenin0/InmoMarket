package com.isanz.inmomarket.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sideSheet: FrameLayout
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
        setUpButtons()
        initializeMobileAds()
        initializeSideFilters()
        loadAdRequest()
        checkAndShowBiometricLoginPopUp()
        setupRecyclerView(homeViewModel)
    }

    private fun setUpButtons() {
        mBinding.filterLayout.btnBack.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun initializeSideFilters() {
        drawerLayout = mBinding.drawerLayout
        sideSheet = mBinding.navView
        mBinding.homeLayout.btnFilter.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun initializeMobileAds() {
        MobileAds.initialize(requireContext()) {}
    }

    private fun loadAdRequest() {
        val adRequest = AdRequest.Builder().build()
        mBinding.homeLayout.adView.loadAd(adRequest)
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
        mBinding.homeLayout.rvHome.adapter = adapter
        mBinding.homeLayout.rvHome.layoutManager = LinearLayoutManager(context)
        observeParcelas(homeViewModel, adapter)
    }

    private fun observeParcelas(homeViewModel: HomeViewModel, adapter: PropertyItemListAdapter) {
        homeViewModel.listParcelas.observe(viewLifecycleOwner) { parcelas ->
            enableFiltersListeners(parcelas, adapter)
            val filteredParcelas = filterParcelas(parcelas)
            updateRecyclerView(filteredParcelas, adapter)
        }
    }

    private fun filterParcelas(parcelas: List<Property>?): List<Property> {
        val minPrice = mBinding.filterLayout.rSPrice.values[0]
        val maxPrice = mBinding.filterLayout.rSPrice.values[1]
        val minMeters = mBinding.filterLayout.rSSqureMeters.values[0]
        val maxMeters = mBinding.filterLayout.rSSqureMeters.values[1]

        return parcelas?.filter {
            it.price.toFloat() in minPrice..maxPrice && it.extras["squareMeters"]!!.toFloat() in minMeters..maxMeters
        } ?: emptyList()
    }

    private fun enableFiltersListeners(parcelas: List<Property>?, adapter: PropertyItemListAdapter) {
        updatePriceFilter(parcelas)
        updateMetersFilter(parcelas)

        mBinding.filterLayout.rSPrice.addOnChangeListener { _, _, _ ->
            val filteredParcelas = filterParcelas(parcelas)
            updateRecyclerView(filteredParcelas, adapter)
        }

        mBinding.filterLayout.rSSqureMeters.addOnChangeListener { _, _, _ ->
            val filteredParcelas = filterParcelas(parcelas)
            updateRecyclerView(filteredParcelas, adapter)
        }
    }

    private fun updateMetersFilter(parcelas: List<Property>?) {

        val minMeters = (parcelas?.mapNotNull { it.extras["squareMeters"] }?.minOrNull()?.toFloat() ?: 0f)
        val maxMeters = (parcelas?.mapNotNull { it.extras["squareMeters"] }?.maxOrNull()?.toFloat() ?: 0f)

        mBinding.filterLayout.rSSqureMeters.valueFrom = minMeters
        mBinding.filterLayout.rSSqureMeters.valueTo = maxMeters
        mBinding.filterLayout.rSSqureMeters.values = listOf(minMeters, maxMeters)
        mBinding.filterLayout.rSSqureMeters.stepSize = 10.0f
    }

    private fun updatePriceFilter(parcelas: List<Property>?) {
        val minPrice = (parcelas?.minOf { it.price }?.toFloat() ?: 0f)
        val maxPrice = (parcelas?.maxOf { it.price }?.toFloat() ?: 0f)

        mBinding.filterLayout.rSPrice.valueFrom = minPrice
        mBinding.filterLayout.rSPrice.valueTo = maxPrice
        mBinding.filterLayout.rSPrice.values = listOf(minPrice, maxPrice)
        mBinding.filterLayout.rSPrice.stepSize = 10000.0f
    }


    private fun updateRecyclerView(parcelas: List<Property>, adapter: PropertyItemListAdapter) {
        adapter.submitList(parcelas)
        if (parcelas.isEmpty()) {
            mBinding.homeLayout.emptyTextView.visibility = View.VISIBLE
        } else {
            mBinding.homeLayout.emptyTextView.visibility = View.GONE
        }
        mBinding.homeLayout.progressBar.visibility = View.GONE
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
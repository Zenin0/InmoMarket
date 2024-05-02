package com.isanz.inmomarket.ui.property

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentMiniPropertyBinding
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class MiniPropertyFragment : DialogFragment() {

    private var propertyId: String? = null
    private lateinit var mBinding: FragmentMiniPropertyBinding
    private val propertyViewModel: PropertyViewModel by lazy {
        ViewModelProvider(this)[PropertyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString("propertyId")
        setUpProperty()
    }

    private fun setUpProperty() {
        lifecycleScope.launch {
            setUp(propertyId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMiniPropertyBinding.inflate(layoutInflater)
        return mBinding.root
    }

    private suspend fun setUp(propertyId: String?) {
        val property = propertyViewModel.retrieveProperty(propertyId!!)
        if (property != null) {
            setPropertyDetails(property)
            setOverlayClickListener(propertyId)
        }
    }

    private fun setPropertyDetails(property: Property) {
        mBinding.tvProperty.text = property.tittle
        "Price: ${property.price} €".also { mBinding.tvPrice.text = it }
        Glide.with(this).load(property.listImagesUri[0]).into(mBinding.ivProperty)
    }

    private fun setOverlayClickListener(propertyId: String) {
        mBinding.vOverlay.setOnClickListener {
            val bundle = Bundle().apply {
                putString("propertyId", propertyId)
            }
            this.findNavController()
                .navigate(R.id.action_navigation_mini_property_to_navigation_property, bundle)
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogLayout()
    }

    private fun setDialogLayout() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        setUpProperty()
    }
}
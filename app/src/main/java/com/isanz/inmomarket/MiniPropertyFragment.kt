package com.isanz.inmomarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.isanz.inmomarket.databinding.FragmentMiniPropertyBinding
import com.isanz.inmomarket.ui.property.PropertyViewModel
import kotlinx.coroutines.launch

class MiniPropertyFragment : DialogFragment() {

    private var propertyId: String? = null

    private lateinit var viewModel: PropertyViewModel

    private var mBinding: FragmentMiniPropertyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString("propertyId")
        viewModel = PropertyViewModel()
        lifecycleScope.launch {
            setUp(propertyId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMiniPropertyBinding.inflate(layoutInflater)
        val binding = mBinding // Introduce a local read-only variable

        return binding?.root ?: View(context)
    }

    private suspend fun setUp(propertyId: String?) {
        val property = viewModel.retrieveProperty(propertyId!!)
        val binding = mBinding // Introduce a local read-only variable
        if (property != null && binding != null) { // Check if binding is not null
            binding.tvProperty.text = property.tittle
            "Price: ${property.price} €".also { binding.tvPrice.text = it }
            Glide.with(this).load(property.listImagesUri[0]).into(binding.ivProperty)
        }
        mBinding?.vOverlay!!.setOnClickListener {
            val bundle = Bundle().apply {
                putString("propertyId", propertyId)
            }
            this.findNavController().navigate(R.id.action_navigation_mini_property_to_navigation_property, bundle)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            setUp(propertyId)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mBinding = null // Nullify your view binding
    }
}
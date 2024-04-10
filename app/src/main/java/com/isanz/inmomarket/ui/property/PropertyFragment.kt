package com.isanz.inmomarket.ui.property

import PropertyViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentPropertyBinding
import com.isanz.inmomarket.rv.extraItem.ExtraListAdapter
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class PropertyFragment : Fragment() {

    private var propertyId: String? = null

    private lateinit var viewModel: PropertyViewModel

    private lateinit var mBinding: FragmentPropertyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString("propertyId")
        viewModel = PropertyViewModel()
        lifecycleScope.launch {
            setUp(propertyId)
        }

    }

    private fun setUpButtons(property: Property) {
        mBinding.btnChat.setOnClickListener {
            viewModel.addChat(InmoMarket.getAuth().currentUser!!.uid, property.userId) { chatId ->
                val bundle = Bundle().apply {
                    putString("idChat", chatId)
                }
                this.findNavController().navigate(R.id.action_propertyFragment_to_chatFragment, bundle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPropertyBinding.inflate(layoutInflater)
        val extraAdapter = ExtraListAdapter("PropertyFragment")
        mBinding.rvExtras.adapter = extraAdapter

        return mBinding.root
    }

    private suspend fun setUp(propertyId: String?) {
        val property = viewModel.retrieveProperty(propertyId!!)
        if (property != null) {
            mBinding.tvProperty.text = property.tittle
            mBinding.tvDescription.text = property.description
            mBinding.tvAddress.text = property.location
            "Price: ${property.price} €".also { mBinding.tvPrice.text = it }
            Glide.with(this).load(property.listImagesUri[0]).into(mBinding.ivProperty)
            // Load extra rv
            loadExtras(property)
        }
        setUpButtons(property!!)
    }

    private fun loadExtras(property: Property?) {
        // Get the adapter from the RecyclerView
        val extraAdapter = mBinding.rvExtras.adapter as ExtraListAdapter

        // Convert the Map to a List of Pairs and submit it to the adapter
        property?.extras?.let { extras ->
            val extrasList = extras.toList()
            extraAdapter.submitList(extrasList)
        }
    }
}
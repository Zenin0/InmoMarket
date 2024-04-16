package com.isanz.inmomarket.ui.property

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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

    private var mBinding: FragmentPropertyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString("propertyId")
        viewModel = PropertyViewModel()
        lifecycleScope.launch {
            setUp(propertyId)
        }

    }

    private fun setUpButtons(property: Property) {
        mBinding?.btnChat?.setOnClickListener {
            viewModel.addChat(InmoMarket.getAuth().currentUser!!.uid, property.userId) { chatId ->
                val bundle = Bundle().apply {
                    putString("idChat", chatId)
                }
                this.findNavController().navigate(R.id.action_propertyFragment_to_chatFragment, bundle)
            }
        }

        val updateFavoriteIcon: (Boolean) -> Unit = { isFavorite ->
            if (isFavorite) {
                mBinding?.ibFavorite?.setImageResource(R.drawable.ic_favorite)
            } else {
                mBinding?.ibFavorite?.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        // Call the getIfFavorite function from the ViewModel
        viewModel.getIfFavorite(property, updateFavoriteIcon)

        // Load the animation
        val rotateAnimation = AnimationUtils.loadAnimation(mBinding?.ibFavorite?.context, R.anim.rotate)

        mBinding?.ibFavorite?.setOnClickListener {
            // Start the animation
            it.startAnimation(rotateAnimation)

            viewModel.alterFavorite(property, updateFavoriteIcon)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPropertyBinding.inflate(layoutInflater)
        val binding = mBinding // Introduce a local read-only variable
        val extraAdapter = ExtraListAdapter("PropertyFragment")
        binding?.rvExtras?.adapter = extraAdapter

        return binding?.root ?: View(context)
    }

    private suspend fun setUp(propertyId: String?) {
        val property = viewModel.retrieveProperty(propertyId!!)
        val binding = mBinding // Introduce a local read-only variable
        if (property != null && binding != null) { // Check if binding is not null
            binding.tvProperty.text = property.tittle
            binding.tvDescription.text = property.description
            binding.tvAddress.text = property.location
            "Price: ${property.price} €".also { binding.tvPrice.text = it }
            Glide.with(this).load(property.listImagesUri[0]).into(binding.ivProperty)
            // Load extra rv
            loadExtras(property)
            setUpButtons(property)
            binding.progressBar.visibility = View.GONE
        }
    }



    private fun loadExtras(property: Property?) {
        // Get the adapter from the RecyclerView
        val extraAdapter = mBinding?.rvExtras?.adapter as ExtraListAdapter

        // Convert the Map to a List of Pairs and submit it to the adapter
        property?.extras?.let { extras ->
            val extrasList = extras.toList()
            extraAdapter.submitList(extrasList)
        }
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
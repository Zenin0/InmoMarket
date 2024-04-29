package com.isanz.inmomarket.ui.property

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentPropertyBinding
import com.isanz.inmomarket.rv.CarouselAdapter
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
        viewModel = ViewModelProvider(this)[PropertyViewModel::class.java]

    }

    private fun setUpButtons(property: Property) {
        mBinding.btnChat.setOnClickListener {
            viewModel.createChat(
                InmoMarket.getAuth().currentUser!!.uid,
                property.userId
            ) { chatId ->
                val bundle = Bundle().apply {
                    putString("idChat", chatId)
                }
                this.findNavController()
                    .navigate(R.id.action_navigation_property_to_chatFragment, bundle)
            }
        }

        val updateFavoriteIcon: (Boolean) -> Unit = { isFavorite ->
            if (isFavorite) {
                mBinding.ibFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                mBinding.ibFavorite.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        viewModel.getIfFavorite(property, updateFavoriteIcon)
        val rotateAnimation =
            AnimationUtils.loadAnimation(mBinding.ibFavorite.context, R.anim.rotate)
        mBinding.ibFavorite.setOnClickListener {
            it.startAnimation(rotateAnimation)
            viewModel.alterFavorite(property, updateFavoriteIcon)
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
            val imageList = property.listImagesUri
            val adapter = CarouselAdapter(imageList)
            this.mBinding.vpProperty.adapter = adapter

            setupIndicators(imageList.size)

            this.mBinding.vpProperty.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentIndicator(position)
                }
            })
            loadExtras(property)
            setUpButtons(property)
            mBinding.progressBar.visibility = View.GONE
        }
    }

    private fun setupIndicators(count: Int) {
        mBinding.indicatorLayout.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(context)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_outline_circle
                    )
                )
                it.layoutParams = layoutParams
                mBinding.indicatorLayout.addView(it)
            }
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = mBinding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = mBinding.indicatorLayout.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_circle
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_outline_circle
                    )
                )
            }
        }
    }


    private fun loadExtras(property: Property?) {
        val extraAdapter = mBinding.rvExtras.adapter as ExtraListAdapter

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

}
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
import com.bumptech.glide.Glide
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentPropertyBinding
import com.isanz.inmomarket.rv.CarouselAdapter
import com.isanz.inmomarket.rv.extraItem.ExtraListAdapter
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.launch

class PropertyFragment : Fragment() {

    private var propertyId: String? = null
    private lateinit var mBinding: FragmentPropertyBinding
    private val propertyViewModel: PropertyViewModel by lazy {
        ViewModelProvider(this)[PropertyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString("propertyId")

    }

    private fun setUpButtons(property: Property) {
        mBinding.btnChat.setOnClickListener {
            propertyViewModel.createChat(
                InmoMarket.getAuth().currentUser!!.uid, property.userId
            ) { chatId ->
                val bundle = Bundle().apply {
                    putString("idChat", chatId)
                }
                this.findNavController()
                    .navigate(R.id.action_navigation_property_to_chatFragment, bundle)
            }
        }

        mBinding.ibBack.setOnClickListener {
            this.findNavController().popBackStack()
        }

        val updateFavoriteIcon: (Boolean) -> Unit = { isFavorite ->
            if (isFavorite) {
                mBinding.ibFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                mBinding.ibFavorite.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        propertyViewModel.getIfFavorite(property, updateFavoriteIcon)
        val rotateAnimation =
            AnimationUtils.loadAnimation(mBinding.ibFavorite.context, R.anim.rotate)
        mBinding.ibFavorite.setOnClickListener {
            it.startAnimation(rotateAnimation)
            propertyViewModel.alterFavorite(property, updateFavoriteIcon)
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

    private fun setUpView(propertyId: String?) {
        propertyViewModel.retrieveProperty(propertyId!!) { property ->
            if (property != null) {
                mBinding.tvProperty.text = property.tittle
                mBinding.tvDescription.text = property.description
                "${property.location.split(",")[0]}, ${property.location.split(",")[1]}, ${
                    property.location.split(
                        ","
                    )[2].split(" ")[1]
                }".also { mBinding.tvAddress.text = it }

                (property.price.toInt().toString() + "€").also { mBinding.tvPrice.text = it }
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
                propertyViewModel.retrieveProfile(property.userId) { user ->
                    if (user != null) {
                        mBinding.tvProfile.text = user.displayName
                        Glide.with(this).load(user.photoUrl).circleCrop().into(mBinding.ivProfile)
                    }
                }
                loadExtras(property)
                setUpButtons(property)
                mBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupIndicators(count: Int) {
        mBinding.indicatorLayout.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
            setUpView(propertyId)
        }
    }

}

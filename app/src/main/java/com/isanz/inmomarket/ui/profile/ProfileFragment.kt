package com.isanz.inmomarket.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.isanz.inmomarket.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding


    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            setUpView()
        }
        setUpButtons()
        return mBinding.root
    }

    private fun setUpButtons() {
        mBinding.btnResetPassword.setOnClickListener {
            profileViewModel.resetPassword()
        }
    }

    private suspend fun setUpView() {
        val user = profileViewModel.retrieveProfile()
        loadImage(mBinding.ivProfile, user.photoUrl!!)
        mBinding.tvName.text = user.displayName

    }

    private fun loadImage(view: ImageView, url: String) {
        Glide.with(view.context).load(url).centerCrop().into(view)
    }

}
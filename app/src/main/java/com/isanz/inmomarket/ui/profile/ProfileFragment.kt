package com.isanz.inmomarket.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentProfileBinding
import com.isanz.inmomarket.ui.portal.login.LoginActivity
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileUserId: String

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        arguments?.let {
            profileUserId = it.getString("profileId") ?: ""
        }
        setUp()
        return mBinding.root
    }

    private fun setUp() {
        lifecycleScope.launch {
            setUpView()
        }
        setUpButtons()
        setUpDrawer()
        setUpTabs()
        setUpProfileImagePicker()
    }

    private fun setUpButtons() {
        mBinding.profileLayout.ibDrawer.setOnClickListener {
            mBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        mBinding.profileLayout.ibBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpProfileImagePicker() {
        mBinding.profileLayout.ivProfile.setOnClickListener {
            launchImagePicker()
        }
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerResultLauncher.launch(intent)
    }

    private val imagePickerResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                handleImagePicked(imageUri)
            }
        }

    private fun handleImagePicked(imageUri: Uri?) {
        if (imageUri != null) {
            loadImage(mBinding.profileLayout.ivProfile, imageUri.toString())
            profileViewModel.updateUserProfilePhoto(imageUri)
        }
    }

    private fun setUpTabs() {
        val tabLayout = mBinding.profileLayout.tabLayout
        val viewPager = mBinding.profileLayout.viewPager



        if (profileUserId.isEmpty()) {
            viewPager.adapter = ViewPagerAdapter(requireActivity())
        } else {
            viewPager.adapter = ViewPagerAdapter(requireActivity(), profileUserId)
        }


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_favorites)
                1 -> tab.text = getString(R.string.tab_uploads)
                else -> error("Invalid position")
            }
        }.attach()
    }

    private fun setUpDrawer() {
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_logout -> {
                    if (profileViewModel.signOut()) {
                        val intent = Intent(activity, LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                }

                R.id.nav_about -> {
                    shareLink(Constants.GITHUB)
                }

                R.id.nav_settings -> {
                    this.findNavController()
                        .navigate(R.id.action_navigation_profile_to_settingsFragment)
                }

                R.id.nav_share -> {
                    shareLink(Constants.GITHUB)
                }

                R.id.nav_plans -> {
                    sendToWeb(Constants.PAYMENTS[Random.nextInt(0, Constants.PAYMENTS.size)])
                }

                R.id.nav_promote -> {
                    sendToWeb(Constants.PAYMENTS[Random.nextInt(0, Constants.PAYMENTS.size)])
                }

            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun shareLink(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        val plansIntent = Intent.createChooser(sendIntent, null)
        startActivity(plansIntent)
    }

    private fun sendToWeb(url: String) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(webIntent)
    }

    private fun setUpView() {
        navView = mBinding.navView
        drawerLayout = mBinding.drawerLayout
        if (profileUserId.isEmpty()) {
            viewProfileForOther()
        } else {
            viewProfileForSelf()
        }
    }

    private fun viewProfileForOther() {
        mBinding.profileLayout.ibDrawer.visibility = View.VISIBLE
        mBinding.profileLayout.ibBack.visibility = View.GONE
        profileViewModel.retrieveProfile { user ->
            if (user != null) {
                loadImage(mBinding.profileLayout.ivProfile, user.photoUrl!!)
                mBinding.profileLayout.tvName.text = user.displayName
            }
        }
    }

    private fun viewProfileForSelf() {
        mBinding.profileLayout.ibDrawer.visibility = View.GONE
        mBinding.profileLayout.ibBack.visibility = View.VISIBLE
        profileViewModel.retrieveProfile(profileUserId) { user ->
            if (user != null) {
                loadImage(mBinding.profileLayout.ivProfile, user.photoUrl!!)
                mBinding.profileLayout.tvName.text = user.displayName
            }
        }
    }

    private fun loadImage(view: ImageView, url: String) {
        try {
            Glide.with(view.context).load(url).circleCrop().into(view)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "loadImage:failure", e)
            e.printStackTrace()
        }
    }

}

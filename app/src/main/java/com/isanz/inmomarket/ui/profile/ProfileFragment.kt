package com.isanz.inmomarket.ui.profile

import ViewPagerAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

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
        setUpDrawer()
        setUpTabs()
        setUpButtons()
        return mBinding.root
    }

    private fun setUpButtons() {
        mBinding.profileLayout.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, Constants.REQUEST_CODE_PICK_IMAGES)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            loadImage(mBinding.profileLayout.ivProfile, imageUri.toString())
            if (imageUri != null) {
                profileViewModel.updateUserProfilePhoto(imageUri)
            }
        }
    }

    private fun setUpTabs() {
        val tabLayout = mBinding.profileLayout.tabLayout
        val viewPager = mBinding.profileLayout.viewPager


        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_favorites)
                1 -> tab.text = getString(R.string.tab_your_uploads)
                else -> throw IllegalStateException("Invalid position")
            }
        }.attach()
    }

    private fun setUpDrawer() {
        mBinding.profileLayout.ibDrawer.setOnClickListener {
            mBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

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
                    val url = "https://github.com/Zenin0/InmoMarket" // replace with your URL
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }

                R.id.nav_settings -> {
                    this.findNavController()
                        .navigate(R.id.action_navigation_profile_to_settingsFragment)
                }

                R.id.nav_share -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "https://github.com/Zenin0/InmoMarket")
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private suspend fun setUpView() {
        navView = mBinding.navView
        drawerLayout = mBinding.drawerLayout
        val user = profileViewModel.retrieveProfile()
        loadImage(mBinding.profileLayout.ivProfile, user.photoUrl!!)
        mBinding.profileLayout.tvName.text = user.displayName

    }

    private fun loadImage(view: ImageView, url: String) {
        Glide.with(view.context).load(url).circleCrop().into(view)
    }

}
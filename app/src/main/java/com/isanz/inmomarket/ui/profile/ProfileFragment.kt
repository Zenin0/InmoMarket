package com.isanz.inmomarket.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.isanz.inmomarket.databinding.FragmentProfileBinding
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
        navView = mBinding.navView
        drawerLayout = mBinding.drawerLayout
        lifecycleScope.launch {
            setUpView()
        }
        setUpButtons()
        setUpDrawer()
        return mBinding.root
    }

    private fun setUpDrawer() {
        mBinding.appBarMain.ibDrawer.setOnClickListener {
            mBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener {
            // Handle navigation view item clicks here.

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setUpButtons() {

        mBinding.appBarMain.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constants.PICK_IMAGE_REQUEST_CODE)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            profileViewModel.uploadImageToFirebase(imageUri!!) { imageUrl ->
                Glide.with(this).load(imageUrl).circleCrop().into(mBinding.appBarMain.ivProfile)
            }
        }
    }

    private suspend fun setUpView() {
        val user = profileViewModel.retrieveProfile()
        loadImage(mBinding.appBarMain.ivProfile, user.photoUrl!!)
        mBinding.appBarMain.tvName.text = user.displayName

    }

    private fun loadImage(view: ImageView, url: String) {
        Glide.with(view.context).load(url).circleCrop().into(view)
    }

}
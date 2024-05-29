package com.isanz.inmomarket.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentAddImagesBinding
import com.isanz.inmomarket.rv.imageItem.ImageListAdapter
import java.util.UUID

class AddImagesFragment : Fragment() {

    private lateinit var mBinding: FragmentAddImagesBinding
    private val addViewModel: AddViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddImagesBinding.inflate(inflater, container, false)
        setUp()
        return mBinding.root
    }

    private fun setUp() {
        setupImageListAdapter()
        setUpButtons()
    }

    private fun setUpButtons() {
        mBinding.btnLoadImages.setOnClickListener {
            loadImages()
        }
        mBinding.btnNext.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        var isValid = true

        if (mBinding.tiePrice.text.toString().isEmpty()) {
            mBinding.tiePrice.error = getString(R.string.price_is_required)
            isValid = false
        }

        val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
        val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()
        if (listImagesUri.size < 3) {
            Toast.makeText(context, getString(R.string.images_3), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (isValid) {
            mBinding.vLoading.visibility = View.VISIBLE
            mBinding.pbLoading.visibility = View.VISIBLE
            saveImagesAndPrice()
        }
    }

    private fun saveImagesAndPrice() {
        val property = addViewModel.property.value!!
        property.price = mBinding.tiePrice.text.toString().toDouble()

        val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
        val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()

        val images = mutableListOf<String>()
        for (uri in listImagesUri) {
            val imageName = UUID.randomUUID().toString()
            val ref =
                InmoMarket.getStorage().reference.child("images/property/${InmoMarket.getAuth().currentUser!!.uid}/$imageName")
            ref.putFile(Uri.parse(uri)).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    images.add(it.toString())
                    if (images.size == listImagesUri.size) {
                        property.listImagesUri = images
                        property.userId = InmoMarket.getAuth().currentUser!!.uid
                        addViewModel.save(property)
                        updateUI()

                    }
                }
            }
        }
    }

    private fun updateUI() {
        mBinding.vLoading.visibility = View.GONE
        mBinding.pbLoading.visibility = View.GONE
        mBinding.checkMark.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToStart()
        }, 2000)
    }

    private fun navigateToStart() {
        findNavController().navigate(R.id.action_navigation_add_images_to_navigation_add_tittle_description)
    }

    private fun loadImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerResultLauncher.launch(intent)
    }

    private val imagePickerResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleImagePickResult(result.data)
            }
        }

    private fun handleImagePickResult(data: Intent?) {
        val imageUris = extractImageUrisFromIntent(data)
        val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
        adapter?.submitList(imageUris)
        mBinding.rvImages.visibility = View.VISIBLE
    }

    private fun extractImageUrisFromIntent(data: Intent?): MutableList<String> {
        val clipData = data?.clipData
        val imageUris = mutableListOf<String>()

        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                imageUris.add(uri.toString())
            }
        } else {
            val uri = data?.data
            if (uri != null) {
                imageUris.add(uri.toString())
            }
        }
        return imageUris
    }

    private fun setupImageListAdapter() {
        val adapter = ImageListAdapter()
        mBinding.rvImages.adapter = adapter
    }


}

package com.isanz.inmomarket.ui.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.isanz.inmomarket.databinding.FragmentAddBinding
import com.isanz.inmomarket.ui.rv.imageItem.ImageListAdapter

class AddFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGES = 1
    }

    private var mBinding: FragmentAddBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[AddViewModel::class.java]

        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the RecyclerView adapter
        val adapter = ImageListAdapter()
        binding.rvImages.adapter = adapter

        setUpButtons()
        return root
    }

    private fun setUpButtons() {
        binding.btnLoadImages.setOnClickListener {
            loadImages()
        }
    }

    private fun loadImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            val imageUris = mutableListOf<String>()

            if (clipData != null) {
                // Multiple images were selected
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    imageUris.add(uri.toString())
                }
            } else {
                // Single image was selected
                val uri = data?.data
                if (uri != null) {
                    imageUris.add(uri.toString())
                }
            }

            // Load the images into the RecyclerView
            val adapter = (binding.rvImages.adapter as? ImageListAdapter)
            adapter?.submitList(imageUris)
            mBinding!!.rvImages.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}
package com.isanz.inmomarket.ui.add

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.FragmentAddBinding
import com.isanz.inmomarket.ui.home.HomeViewModel
import com.isanz.inmomarket.ui.rv.imageItem.ImageListAdapter
import com.isanz.inmomarket.utils.Constants
import java.util.UUID

@Suppress("DEPRECATION")
class AddFragment : Fragment() {


    private lateinit var mBinding: FragmentAddBinding

    private lateinit var db: FirebaseFirestore

    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = mBinding.root
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        db = InmoMarket.getDb()

        // Initialize the RecyclerView adapter
        val adapter = ImageListAdapter()
        mBinding.rvImages.adapter = adapter

        setUpButtons()
        setUpDrawables()
        return root
    }

    private fun setUpDrawables() {
        val drawableBath =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_bathroom)
        val drawableRooms =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_bedroom)
        mBinding.tieBats.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableBath, null)
        mBinding.tieRooms.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRooms, null)
    }

    private fun setUpButtons() {
        mBinding.btnLoadImages.setOnClickListener {
            loadImages()
        }

        mBinding.btnSave.setOnClickListener {
            save()
        }
    }

    private fun save() {
        // Disable screen elements and show progress bar
        mBinding.view.visibility = View.VISIBLE
        mBinding.progressBar.visibility = View.VISIBLE


        val tittle = mBinding.tieTittle.text.toString()
        val description = mBinding.tieDescription.text.toString()
        val location = mBinding.tieAddress.text.toString()
        val baths = mBinding.tieBats.text.toString().toIntOrNull() ?: 0
        val rooms = mBinding.tieRooms.text.toString().toIntOrNull() ?: 0

        if (validateFields(tittle, description, location, baths, rooms)) {

            // Get the list of images URIs
            val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
            val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()

            val storage = InmoMarket.getStorage()
            val storageRef = storage.reference

            val downloadUrls = mutableListOf<String>()

            for (uri in listImagesUri) {
                val imageRef = storageRef.child("images/${UUID.randomUUID()}")
                val uploadTask = imageRef.putFile(Uri.parse(uri))

                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        downloadUrls.add(downloadUri.toString())

                        if (downloadUrls.size == listImagesUri.size) {
                            val parcela = hashMapOf(
                                "userId" to InmoMarket.getUserAuth()?.uid,
                                "tittle" to tittle,
                                "description" to description,
                                "location" to location,
                                "listImagesUri" to downloadUrls,
                                "baths" to baths,
                                "rooms" to rooms
                            )

                            db.collection("parcelas").add(parcela)
                                .addOnSuccessListener { documentReference ->
                                    Log.i(
                                        TAG,
                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                    )
                                    updateUI("Property added successfully")
                                    homeViewModel.updateParcelas()


                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Error adding document", e)
                                    updateUI("Error adding property")
                                }
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading image", e)
                }
            }
        } else {
            mBinding.view.visibility = View.GONE
            mBinding.progressBar.visibility = View.GONE
        }
    }

    private fun validateFields(
        tittle: String, description: String, location: String, baths: Int, rooms: Int
    ): Boolean {
        if (tittle.isEmpty()) {
            mBinding.tieTittle.error = "Tittle is required"
            return false
        }
        if (tittle.length < 5) {
            mBinding.tieTittle.error = "Tittle must be at least 5 characters"
            return false
        }
        if (description.isEmpty()) {
            mBinding.tieDescription.error = "Description is required"
            return false
        }
        if (description.length < 20) {
            mBinding.tieDescription.error = "Description must be at least 20 characters"
            return false
        }
        if (location.isEmpty()) {
            mBinding.tieAddress.error = "Location is required"
            return false
        }
        if (baths == 0) {
            mBinding.tieBats.error = "Baths is required"
            return false
        }
        if (rooms == 0) {
            mBinding.tieRooms.error = "Rooms is required"
            return false
        }
        val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
        val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()
        if (listImagesUri.size < 3) {
            Toast.makeText(context, "At least 3 images are required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateUI(message: String) {
        mBinding.tieTittle.text?.clear()
        mBinding.tieDescription.text?.clear()
        mBinding.tieAddress.text?.clear()
        mBinding.tieBats.text?.clear()
        mBinding.tieRooms.text?.clear()
        (mBinding.rvImages.adapter as? ImageListAdapter)?.submitList(emptyList())
        mBinding.rvImages.visibility = View.GONE
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        mBinding.view.visibility = View.GONE
        mBinding.progressBar.visibility = View.GONE

    }

    private fun loadImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_PICK_IMAGES)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
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
            val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
            adapter?.submitList(imageUris)
            mBinding.rvImages.visibility = View.VISIBLE
        }
    }
}
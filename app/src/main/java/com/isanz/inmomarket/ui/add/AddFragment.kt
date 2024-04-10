package com.isanz.inmomarket.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.isanz.inmomarket.rv.imageItem.ImageListAdapter
import com.isanz.inmomarket.utils.Constants
import java.util.UUID

@Suppress("DEPRECATION")
class AddFragment : Fragment() {


    private lateinit var mBinding: FragmentAddBinding

    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = mBinding.root
        db = InmoMarket.getDb()

        // Initialize the RecyclerView adapter
        val adapter = ImageListAdapter()
        mBinding.rvImages.adapter = adapter

        setUpButtons()
        setUpDrawables()
        return root
    }

    private fun setUpDrawables() {
        val drawableBath = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bathroom)
        val drawableRooms = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bedroom)
        val drawableMeters = ContextCompat.getDrawable(requireContext(), R.drawable.ic_square_foot)
        val drawableFloor = ContextCompat.getDrawable(requireContext(), R.drawable.ic_house_siding)
        val drawablePrice = ContextCompat.getDrawable(requireContext(), R.drawable.ic_euro_symbol)
        mBinding.tieBats.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableBath, null)
        mBinding.tieRooms.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRooms, null)
        mBinding.tieSquareMeters.setCompoundDrawablesWithIntrinsicBounds(
            null, null, drawableMeters, null
        )
        mBinding.tieFloors.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableFloor, null)
        mBinding.tiePrice.setCompoundDrawablesWithIntrinsicBounds(null, null, drawablePrice, null)
    }

    private fun setUpButtons() {
        mBinding.btnLoadImages.setOnClickListener {
            loadImages()
        }

        mBinding.btnSave.setOnClickListener {
            save(addViewModel = ViewModelProvider(this)[AddViewModel::class.java])
        }
    }

    private fun save(addViewModel: AddViewModel) {
        val tittle = mBinding.tieTittle.text.toString()
        val description = mBinding.tieDescription.text.toString()
        val location = mBinding.tieAddress.text.toString()
        val baths = mBinding.tieBats.text.toString().toIntOrNull() ?: 0
        val rooms = mBinding.tieRooms.text.toString().toIntOrNull() ?: 0
        val squareMeters = mBinding.tieSquareMeters.text.toString().toIntOrNull() ?: 0
        val floors = mBinding.tieFloors.text.toString().toIntOrNull() ?: 0
        val price = mBinding.tiePrice.text.toString().toIntOrNull() ?: 0

        if (validateFields(tittle, description, location, baths, rooms)) {
            mBinding.view.visibility = View.VISIBLE
            mBinding.progressBar.visibility = View.VISIBLE

            val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
            val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()

            val images = mutableListOf<String>()
            for (uri in listImagesUri) {
                val imageName = UUID.randomUUID().toString()
                val ref =
                    InmoMarket.getStorage().reference.child("images/${InmoMarket.getAuth().currentUser!!.uid}/$imageName")
                val extras = hashMapOf(
                    "rooms" to rooms,
                    "baths" to baths,
                    "squareMeters" to squareMeters,
                    "floors" to floors
                )
                ref.putFile(Uri.parse(uri)).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        images.add(it.toString())
                        if (images.size == listImagesUri.size) {
                            updateUI(
                                addViewModel.save(
                                    tittle,
                                    description,
                                    location,
                                    images,
                                    extras,
                                    price.toDouble(),
                                    squareMeters.toDouble()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validateFields(
        tittle: String, description: String, location: String, baths: Int, rooms: Int
    ): Boolean {
        if (tittle.isEmpty()) {
            mBinding.tieTittle.error = getString(R.string.tittle_is_required)
            return false
        }
        if (tittle.length < 5) {
            mBinding.tieTittle.error = getString(R.string.tittle_must_be_at_least_5_characters)
            return false
        }
        if (description.isEmpty()) {
            mBinding.tieDescription.error = getString(R.string.description_is_required)
            return false
        }
        if (description.length < 20) {
            mBinding.tieDescription.error = getString(R.string.description_least_20)
            return false
        }
        if (location.isEmpty()) {
            mBinding.tieAddress.error = getString(R.string.location_is_required)
            return false
        }
        if (baths == 0) {
            mBinding.tieBats.error = getString(R.string.baths_is_required)
            return false
        }
        if (rooms == 0) {
            mBinding.tieRooms.error = getString(R.string.rooms_is_required)
            return false
        }
        val adapter = (mBinding.rvImages.adapter as? ImageListAdapter)
        val listImagesUri = adapter?.currentList?.map { it } ?: emptyList()
        if (listImagesUri.size < 3) {
            Toast.makeText(context, getString(R.string.images_3), Toast.LENGTH_SHORT).show()
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
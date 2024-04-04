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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.databinding.FragmentAddBinding
import com.isanz.inmomarket.ui.rv.imageItem.ImageListAdapter
import java.util.UUID

@Suppress("DEPRECATION")
class AddFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGES = 1
    }

    private var mBinding: FragmentAddBinding? = null
    private val binding get() = mBinding!!

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[AddViewModel::class.java]

        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db = InmoMarket.getDb()

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

        binding.btnSave.setOnClickListener {
            save()
        }
    }

    private fun save() {
        val tittle = binding.tieTittle.text.toString()
        val description = binding.tieDescription.text.toString()
        val location = binding.tieAddress.text.toString()

        // Get the list of images URIs
        val adapter = (binding.rvImages.adapter as? ImageListAdapter)
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
                            "listImagesUri" to downloadUrls
                        )

                        db.collection("parcelas").add(parcela)
                            .addOnSuccessListener { documentReference ->
                                Log.i(
                                    TAG,
                                    "DocumentSnapshot added with ID: ${documentReference.id}"
                                )
                            }.addOnFailureListener { e ->
                            Log.e(TAG, "Error adding document", e)
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image", e)
            }
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
package com.isanz.inmomarket.ui.portal.register

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.MainActivity
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.ActivityRegisterBinding
import com.isanz.inmomarket.ui.portal.PortalViewModel
import com.isanz.inmomarket.ui.portal.login.LoginActivity
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Random


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private val portalViewModel: PortalViewModel by lazy {
        ViewModelProvider(this)[PortalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        db = InmoMarket.getDb()
        auth = InmoMarket.getAuth()
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleActivityResult(result)
            }
        setUpButtons()
    }

    private fun handleActivityResult(result: ActivityResult) {
        if (result.resultCode == 0 || result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(Constants.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun setUpButtons() {
        setContentView(mBinding.root)
        mBinding.btnRegister.setOnClickListener {
            val email = mBinding.tieEmail.text.toString()
            val password = mBinding.tiePassword.text.toString()
            createUserWithEmail(email, password)
        }
        mBinding.btnSignInGoogle.setOnClickListener {
            registerGoogle()
        }
        mBinding.flAlreadyHaveAccount.setOnClickListener {
            goToLogin()
        }
        lifecycleScope.launch {
            setImage(mBinding.ivLogo)
        }

    }

    private fun registerGoogle() {
        val signInIntent = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        ).signInIntent

        startForResult.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(user!!.email.toString().split("@")[0].substring(0, 8))
                        .build()
                    user.updateProfile(profileUpdates).addOnCompleteListener { taskProf ->
                        if (taskProf.isSuccessful) {
                            saveUserToFirestore(user)
                            goToMain(user)
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Log.w(Constants.TAG, "googleSign:failure", e)
        }
    }

    private fun createUserWithEmail(email: String, password: String) {
        val result = checkFields(email, password)
        if (result.second) {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        handleUserCreation(task)
                    }
            } catch (e: Exception) {
                Log.w(Constants.TAG, "createUserWithEmail:failure", e)
            }
        }
    }

    private fun handleUserCreation(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            val username = user!!.email.toString().split("@")[0]
            val displayName = if (username.length >= 8) username.substring(0, 8) else username
            val drawable = createImageForProfile(user.email.toString())
            val bitmap = convertDrawableToBitmap(drawable)
            val data = convertBitmapToByteArray(bitmap)
            uploadImageToFirebase(user, data, displayName)
        } else {
            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertDrawableToBitmap(drawable: Drawable): Bitmap {
        return (drawable as BitmapDrawable).bitmap
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    private fun uploadImageToFirebase(user: FirebaseUser, data: ByteArray, displayName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/users/${user.uid}.jpg")
        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnFailureListener {
        }.addOnSuccessListener {
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                updateUserProfile(user, uri, displayName)
            }
        }
    }

    private fun updateUserProfile(user: FirebaseUser, uri: Uri, displayName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { taskUpdate ->
            if (taskUpdate.isSuccessful) {
                saveUserToFirestore(user)
                goToMain(user)
            }
        }
    }

    private fun checkFields(email: String, password: String): Pair<String, Boolean> {
        if (email.isEmpty()) {
            mBinding.tieEmail.error = "Email is required"
            return "Email is empty" to false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mBinding.tieEmail.error = "Email is not valid address"
            return "Email is not valid $email" to false
        }
        if (password.isEmpty()) {
            mBinding.tiePassword.error = "Password is required"
            return "Password is empty" to false
        }
        if (password.length < 6) {
            mBinding.tiePassword.error = "Password must be at least 6 characters"
            return "Password is less than 6 characters" to false
        }
        if (!password.matches(".*\\d.*".toRegex())) {
            mBinding.tiePassword.error = "Password must have at least one digit"
            return "Password must have at least one digit" to false
        }
        if (!password.matches(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*".toRegex())) {
            mBinding.tiePassword.error = "Password must have at least one special character"
            return "Password must have at least one special character" to false
        }
        return "User created successfully" to true
    }

    private fun saveUserToFirestore(user: FirebaseUser?) {
        try {
            user?.let {
                val userMap = hashMapOf(
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl
                )
                db.collection("users").document(user.uid).set(userMap)
            }
        } catch (e: Exception) {
            Log.w(Constants.TAG, "saveUserToFirestore:failure", e)
        }
    }

    private fun goToMain(user: FirebaseUser?) {
        user?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createImageForProfile(email: String): Drawable {
        val firstLetter = email[0].uppercaseChar().toString()

        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw a circle with a random color
        val paint = Paint()
        val rnd = Random()
        paint.color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)

        // Draw the first letter of the email
        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(firstLetter, (size / 2).toFloat(),
            (size / 2 - (paint.descent() + paint.ascent()) / 2), paint)

        return BitmapDrawable(resources, bitmap)
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        finish()
    }

    private fun setImage(view: ImageView) {
        Glide.with(this).load(portalViewModel.getImageRandom()).centerCrop().into(view)
    }
}

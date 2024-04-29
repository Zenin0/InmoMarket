package com.isanz.inmomarket.ui.portal.register

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.MainActivity
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.ActivityRegisterBinding
import com.isanz.inmomarket.ui.portal.PortalViewModel
import com.isanz.inmomarket.ui.portal.login.LoginActivity
import com.isanz.inmomarket.utils.Constants
import kotlinx.coroutines.launch


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private lateinit var viewModel: PortalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        db = InmoMarket.getDb()
        auth = InmoMarket.getAuth()
        viewModel = PortalViewModel()
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        Log.w(TAG, "Google sign in failed", e)
                    }
                }
            }
        setUpButtons()
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
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user!!.email.toString().split("@")[0].substring(0, 8)).build()
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
    }

    private fun createUserWithEmail(email: String, password: String) {
        val result = checkFields(email, password)
        if (result.second.not()) {
            return
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val username = user!!.email.toString().split("@")[0]
                        val displayName = if (username.length >= 8) username.substring(0, 8) else username

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(Constants.DEFAULT_IMAGE.toUri())
                            .setDisplayName(displayName)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener { taskUpdate ->
                            if (taskUpdate.isSuccessful) {
                                saveUserToFirestore(user)
                                goToMain(user)
                            }
                        }

                    } else {
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
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
        user?.let {
            val userMap = hashMapOf(
                "email" to user.email,
                "displayName" to user.displayName ,
                "photoUrl" to user.photoUrl
            )
            db.collection("users").document(user.uid).set(userMap)
        }
    }

    private fun goToMain(user: FirebaseUser?) {
        user?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @Suppress("DEPRECATION")
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)

        finish()
    }

    private suspend fun setImage(view: ImageView) {
        Glide.with(this).load(viewModel.getImageRandom()).centerCrop().into(view)
    }
}
package com.isanz.inmomarket.ui.portal.login

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import com.isanz.inmomarket.InmoMarket
import com.isanz.inmomarket.MainActivity
import com.isanz.inmomarket.R
import com.isanz.inmomarket.databinding.ActivityLoginBinding
import com.isanz.inmomarket.ui.portal.PortalViewModel
import com.isanz.inmomarket.ui.portal.register.RegisterActivity
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private val portalViewModel: PortalViewModel by lazy {
        ViewModelProvider(this)[PortalViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        this.auth = InmoMarket.getAuth()
        this.db = FirebaseFirestore.getInstance()
        val sharedPref = this.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("biometricLogin", false)) {
            showBiometricPrompt()
        }
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
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            // Handle the case where the result code is not RESULT_OK
            Toast.makeText(this, "Google sign in was not completed.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setUpButtons() {
        setContentView(mBinding.root)
        mBinding.btnRegister.setOnClickListener {
            val email = mBinding.tieEmail.text.toString()
            val password = mBinding.tiePassword.text.toString()
            signInUserWithEmail(email, password)
        }
        mBinding.btnSignInGoogle.setOnClickListener {
            signInGoogle()
        }
        mBinding.flAlreadyHaveAccount.setOnClickListener {
            goToRegister()
        }

        lifecycleScope.launch {
            setImage(mBinding.ivLogo)
        }

    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password").build()

        val biometricPrompt = BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        Toast.makeText(
                            applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT
                        ).show()
                        goToMain(currentUser)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "No user is currently logged in. Please log in first.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun signInGoogle() {
        try {
            val signInIntent = GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)).requestEmail()
                    .build()
            ).signInIntent
            startForResult.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed", e)
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "ID Token: $idToken")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
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
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInWithCredential:failure", e)
            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInUserWithEmail(email: String, password: String) {
        val result = checkFields(email, password)
        if (result.second) {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        handleSignIn(task)
                    }
            } catch (e: Exception) {
                Log.w(TAG, "signInWithEmailAndPassword:failure", e)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignIn(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            goToMain(user)
        } else {
            Toast.makeText(
                baseContext,
                "Authentication failed.",
                Toast.LENGTH_SHORT,
            ).show()
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
            Log.w(TAG, "Error saving user to Firestore", e)
        }
    }

    private fun goToMain(user: FirebaseUser?) {
        user?.let {
            it.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "An error occurred. Please log in again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    @Suppress("DEPRECATION")
    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private suspend fun setImage(view: ImageView) {
        try {
            Glide.with(this).load(portalViewModel.getImageRandom()).centerCrop().into(view)
        } catch (e: Exception) {
            Log.w(TAG, "Error loading image", e)
        }
    }
}
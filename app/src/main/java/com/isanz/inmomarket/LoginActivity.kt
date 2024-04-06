package com.isanz.inmomarket

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.isanz.inmomarket.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        this.auth = InmoMarket.getAuth()
        showBiometricPrompt()
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
        // Register Button
        mBinding.btnRegister.setOnClickListener {
            val email = mBinding.tieEmail.text.toString()
            val password = mBinding.tiePassword.text.toString()
            signInUserWithEmail(email, password)
        }
        mBinding.btnSignInGoogle.setOnClickListener {
            signInGoogle()
        }
        mBinding.tvAlreadyHaveAccount.setOnClickListener {
            goToRegister()
        }

    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    goToMain(auth.currentUser)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun signInGoogle() {
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
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser
                goToMain(user)
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInUserWithEmail(email: String, password: String) {
        val result = checkFields(email, password)
        // Result must be true, if not send to Log the error message and set to false
        if (result.second.not()) {
            Log.i(TAG, result.first)
            return
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI change activity
                    Log.d(TAG, "signInUserWithEmail:success")
                    val user = auth.currentUser
                    goToMain(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInUserWithEmail:failure", task.exception)
                    // Display error message
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    goToMain(null)
                }
            }
        }

    }

    private fun checkFields(email: String, password: String): Pair<String, Boolean> {
        // Check if the email is empty
        if (email.isEmpty()) {
            mBinding.tieEmail.error = "Email is required"
            return "Email is empty" to false
        }
        // Check if the email is valid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mBinding.tieEmail.error = "Email is not valid address"
            return "Email is not valid $email" to false
        }
        // Check if the password is empty
        if (password.isEmpty()) {
            mBinding.tiePassword.error = "Password is required"
            return "Password is empty" to false
        }
        // Check if the password is less than 6 characters
        if (password.length < 6) {
            mBinding.tiePassword.error = "Password must be at least 6 characters"
            return "Password is less than 6 characters" to false
        }
        // Check if the password has at least one digit
        if (!password.matches(".*\\d.*".toRegex())) {
            mBinding.tiePassword.error = "Password must have at least one digit"
            return "Password must have at least one digit" to false
        }
        // Check if the password has at least one special character
        if (!password.matches(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*".toRegex())) {
            mBinding.tiePassword.error = "Password must have at least one special character"
            return "Password must have at least one special character" to false
        }
        return "User created successfully" to true
    }

    private fun goToMain(user: FirebaseUser?) {
        user?.let {

            // Start the new activity (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // End the current activity (LoginActivity)
            finish()
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
}
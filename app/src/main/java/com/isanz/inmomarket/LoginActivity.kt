package com.isanz.inmomarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.isanz.inmomarket.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.auth = InnmoMarket.getAuth()
        setUpButtons()
    }

    private fun setUpButtons() {
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Register Button
        mBinding.btnRegister.setOnClickListener {
            val email = mBinding.tieEmail.text.toString()
            val password = mBinding.tiePassword.text.toString()
            createUserWithEmail(email, password)
        }
    }


    private fun createUserWithEmail(email: String, password: String) {
        val result = checkFields(email, password)
        // Result must be true, if not send to Log the error message and set to false
        if (result.second.not()) {
            Log.i(TAG, result.first)
            return
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI change activity
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        // Display error message
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
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

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            // Start the new activity (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // End the current activity (LoginActivity)
            finish()
        }
    }
}
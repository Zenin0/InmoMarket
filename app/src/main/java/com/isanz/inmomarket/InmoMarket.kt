package com.isanz.inmomarket

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class InmoMarket : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        fun getDb(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        fun getUserAuth(): FirebaseUser? {
            return Firebase.auth.currentUser
        }

        fun getStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }

        const val RC_SIGN_IN = 9001

        fun getAuth(): FirebaseAuth {
            return Firebase.auth
        }
    }
}
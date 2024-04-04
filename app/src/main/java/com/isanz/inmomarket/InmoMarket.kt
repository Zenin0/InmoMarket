package com.isanz.inmomarket

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class InmoMarket {

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
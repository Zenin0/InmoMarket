package com.isanz.inmomarket

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class InnmoMarket {

    companion object {
        fun getDb(): FirebaseFirestore {
            return Firebase.firestore
        }

        fun getAuth(): FirebaseAuth {
            return Firebase.auth
        }
    }
}
package com.isanz.inmomarket

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class InnmoMarket {

    fun getDb(): FirebaseFirestore {
        return Firebase.firestore
    }
}
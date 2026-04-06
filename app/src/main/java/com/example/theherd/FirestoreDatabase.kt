package com.example.theherd

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.content.Context

object FirestoreDatabase {

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }// this creates the database connection


    // Collections (Tables)
    val users = db.collection("users")
    val topics = db.collection("topics")

}
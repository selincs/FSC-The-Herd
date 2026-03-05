package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDatabase {

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }// thiss creates the database connection


    //tables for
    val users = db.collection("users")
    val profiles = db.collection("profiles")
    val guides = db.collection("guides")
    val communityBoard = db.collection("Ccommunityboard")

}
package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDatabase {

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }// this creates the database connection


    // Collections (Tables)
    val users = db.collection("users")
    val topics = db.collection("topics")

    //Create subcollection in topic Document for "Community Board Posts"
    fun posts(topicId: String) =
        topics.document(topicId).collection("posts")

}
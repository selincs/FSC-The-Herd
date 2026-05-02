package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object StatusRepository {
    val posts = mutableListOf<Status>()

//    fun addStatus(content: String) {
//        posts.add(0, PostMain(content)) // newest on top
//    }

fun addStatus(content: String) {
    val post = Status(content)

    // 1. Add locally
    posts.add(0, post)

    // 2. Save to Firestore
    val userId = SessionManager.requireUserId()
    val db = FirebaseFirestore.getInstance()

    val postMap = hashMapOf(
        "content" to post.content,
        "timestamp" to post.timestamp
    )

    db.collection("users")
        .document(userId)
        .collection("statusPosts")
        .add(postMap)
}

}
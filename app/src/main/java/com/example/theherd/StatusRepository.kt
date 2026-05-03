package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object StatusRepository {

    fun addStatus(content: String) {
        val userId = SessionManager.requireUserId()
        val db = FirebaseFirestore.getInstance()

        val post = Status(content)

        val postMap = hashMapOf(
            "content" to post.content,
            "timestamp" to post.timestamp
        )

        // Save to subcollection in User Document in Firestore
        db.collection("users")
            .document(userId)
            .collection("statusPosts")
            .add(postMap)
            .addOnSuccessListener {
                println("Saving to StatusPosts subcollection in user doc in FS")

                // ALSO update latestStatusPost field in User document for Friends List fast reads
                db.collection("users")
                    .document(userId)
                    .update("latestStatusPost", postMap)

            }
            .addOnFailureListener { e ->
                println("Failed to save status post: ${e.message}")
            }
    }

}
package com.example.theherd

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID
import com.google.firebase.firestore.Query
import Model.Post

object PostRepository {

    private const val USE_FIRESTORE = true;
    private val auth : FirebaseAuth = FirebaseAuth.getInstance();

    fun createPost(
        topicID: String,
        postTitle: String,
        postContents: String,
        onDone: (Boolean) -> Unit
    ){
        // if there is no user , stop immediately
        val userID = auth.currentUser?.uid ?: run{
            onDone(false)
            return
        }

        val postID = UUID.randomUUID().toString() // generating random post id

        val email = auth.currentUser?.email ?: "unknown"
        val displayName = if (email.contains("@")) {
            email.substringBefore("@")
        } else {
            "User"
        }

        //initalizing the values to the parameters
        val postData = hashMapOf(
            "postID" to postID,
            "postedByUID" to userID,
            "displayName" to displayName,
            "postTitle" to postTitle,
            "postContents" to postContents,
            "likeCount" to 0,
            "commentCount" to 0,
            "postDateTime" to FieldValue.serverTimestamp()
        )

        FirestoreDatabase.topics
            .document(topicID)
            .collection("posts")
            .document(postID)
            .set(postData)
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                onDone(false)
            }



    }

    fun getPosts(
        topicID: String,
        onResult: (List<Post>) -> Unit
    ) {
        FirestoreDatabase.topics
            .document(topicID)
            .collection("posts")
            .get()
            .addOnSuccessListener { result ->

                val sortedDocs = result.documents.sortedWith(
                    compareByDescending<com.google.firebase.firestore.DocumentSnapshot> {
                        it.getBoolean("isPinned") == true
                    }.thenByDescending {
                        it.getTimestamp("postDateTime")
                            ?: it.getTimestamp("postedAt")
                    }
                )

                val postsList = mutableListOf<Post>()

                for (doc in sortedDocs) {
                    val postID = doc.getString("postID") ?: doc.id

                    val rawName = doc.getString("displayName")
                        ?: doc.getString("posterID")
                        ?: "User"

                    val displayName = if (rawName.contains("@")) {
                        rawName.substringBefore("@")
                    } else {
                        rawName
                    }

                    val postTitle = doc.getString("postTitle") ?: ""

                    val postContents = doc.getString("postContents")
                        ?: doc.getString("postText")
                        ?: doc.getString("content")
                        ?: ""

                    val likeCount = doc.getLong("likeCount")?.toInt() ?: 0

                    postsList.add(
                        Post(displayName, postID, postTitle, postContents, likeCount)
                    )
                }

                onResult(postsList)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

}
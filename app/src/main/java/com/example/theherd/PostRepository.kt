package com.example.theherd

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID
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

        //initalizing the values to the parameters
        val postData = hashMapOf(
            "postID" to postID,
            "postedByUID" to userID,
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
            .addOnSuccessListener { result  ->
                val postsList = mutableListOf<Post>()

                for(doc in result ){

                    val postID = doc.getString("postID") ?: continue
                    val postedByUID = doc.getString("postedByUID" ) ?: ""
                    val postTitle = doc.getString("postTitle") ?: ""
                    val postContents= doc.getString("postContents") ?: ""
                    val likeCount = doc.getLong("likeCount")?.toInt() ?: 0


                    val post = Post(
                        postedByUID,
                        postID,
                        postTitle,
                        postContents,
                        likeCount,

                    )
                    postsList.add(post)
                }
                onResult(postsList) // show post list of connected to fire base

            }
            .addOnFailureListener {
                onResult(emptyList()) // show empty list if connection is unavaiable
            }




    }

}
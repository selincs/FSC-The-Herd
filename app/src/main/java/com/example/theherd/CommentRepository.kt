package com.example.theherd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID


object CommentRepository{
    private const val USE_FIRESTORE = true

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    fun createComment(

        topicID : String,
        postId : String,
        commmcontents: String,
        onDone : (Boolean) -> Unit
    ){

        // checking if a user exit.
        val userID = auth.currentUser?.uid ?: run {
            onDone(false)
            return
        }

        //generating a comment id

        val commentID = UUID.randomUUID().toString()

        //comment data map

        val commentData = hashMapOf(
            "commentID" to commentID,
            "commentedByUID"  to userID,
            "commContents" to commmcontents,
            "parentCommentID" to "", // not every comment is a reply,
            "likeCt" to 0,
            "createdAt" to FieldValue.serverTimestamp()


        )
        //firebase path subcollection
        FirestoreDatabase.topics
            .document(topicID)
            .collection("posts")
            .document(postId)
            .collection("comments")// creates new subcollection to post.
            .document(commentID)
            .set(commentData)
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                onDone(false)
            }






    }



}
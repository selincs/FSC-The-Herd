package com.example.theherd
import Model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID


object CommentRepository{
    private const val USE_FIRESTORE = true

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    fun createComment(

        topicID : String,
        postId : String,
        commmContents: String,
        onDone : (Boolean) -> Unit
    ){

        // checking if a user exit.
        val userID = auth.currentUser?.uid ?: run {
            onDone(false)
            return
        }

        //generating a comment id

        val commentID = UUID.randomUUID().toString()

        val email = auth.currentUser?.email ?: "unknown"

        //comment data map

        val commentData = hashMapOf(
            "commentID" to commentID,
            "commentedByUID"  to userID,
            "commentedByEmail" to email,
            "commContents" to commmContents,
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
                //pointing to the location in firestore.
                // this is to count the number of comments
                FirestoreDatabase.topics
                    .document(topicID)
                    .collection("posts")
                    .document(postId)
                    .update("commentCount", FieldValue.increment(1))
                    .addOnSuccessListener {
                        onDone(true)
                    }
                    .addOnFailureListener {
                        onDone(false)
                    }



            }
            .addOnFailureListener {
                onDone(false)
            }






    }

    // this functions gets all the documents and covert it into a comment object to a list.
    fun getComments(
        topicID: String,
        postId: String,
        onResult:(List<Comment>) -> Unit
    ){
        FirestoreDatabase.topics
            .document(topicID)
            .collection("posts")
            .document(postId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val commentsList = mutableListOf<Comment>()

                for( doc in result){
                    val commentID = doc.getString("commentID") ?: continue
                    val commmContents = doc.getString("commContents") ?: ""
                    val commentedByUUID = doc.getString("commentedByUID") ?: continue
                    val email = doc.getString("commentedByEmail") ?: ""
                    val parentCommentID = doc.getString("parentCommentID") ?: ""
                    val likeCt = doc.getLong("likeCt")?.toInt() ?: 0
                    val displayName = if (email.contains("@")) {
                        email.substringBefore("@")
                    } else {
                        commentedByUUID
                    }

                    val comment = Comment(
                        commentID,
                        displayName,
                        commmContents,
                        likeCt,
                        parentCommentID
                    )
                    //each documents gets turned into a comment object and stored into the list.
                    commentsList.add(comment)



                }
                onResult(commentsList)


            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }



}
package com.example.theherd

import Model.Topic
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

//Firestore Topic access is done through FirestoreDatabase.topics
object TopicRepository {
    private const val USE_FIRESTORE = true  //what does this do?
    private const val SYSTEM_USER = "system"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() //Call the singleton Firebase db instance

    //TODO: Only createTopic() is properly implemented. Revisit all. joinTopic() must update 3 documents in Firestore in a BATCH
    //TODO: WRITE, or else race conditions/write failures can be a problem. FS Docs for joinTopic update below
    /*
    topics/topicID/members/userID
    users/userID/joinedTopics/topicID
    topics/topicID/memberCount
     */

    //Create Topic atomically via batch
    fun createTopic(
        topicName: String,
        topicDesc: String,
        imageUri: Uri?,
        creatorID: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        // Step 1: Prevent duplicate topic names
        db.collection("topics")
            .whereEqualTo("topicName", topicName.trim())
            .get()
            .addOnSuccessListener { query ->

                if (!query.isEmpty) {
                    onFailure(Exception("Topic name already exists"))
                    return@addOnSuccessListener
                }

//                val topicRef = db.collection("topics").document() old version of below 2 lines
//                val topicID = topicRef.id
                //Use Topic Name as Unique Identifier (Only 1 Topic of this name Exists)
                val topicID = topicName.trim().lowercase()
                val topicRef = db.collection("topics").document(topicID)

                //But Not for posts or comments, many cases of identical content could exist
                val rulesPostRef = topicRef.collection("posts").document()
                val postID = rulesPostRef.id

                val rulesCommentRef =
                    rulesPostRef.collection("comments").document()

                val batch = db.batch()
                // ------------------------
                // Topic document - trim() prevents white space duplication of Topic Names on save to FS
                // ------------------------
                val topicData = hashMapOf(
                    "topicName" to topicName.trim(),
                    "topicDesc" to topicDesc.trim(),
                    "imageUri" to imageUri?.toString(),
                    "creatorID" to creatorID,
                    "memberCount" to 1,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "isArchived" to false
                )
                batch.set(topicRef, topicData)
                // ------------------------
                // Creator membership - Add the Topic creator as a member & moderator of the Topic
                // ------------------------
                val memberRef = topicRef
                    .collection("members")
                    .document(creatorID)
                val memberData = hashMapOf(
                    "topicID" to topicID,
                    "role" to "moderator",
                    "joinedAt" to FieldValue.serverTimestamp()
                )
                batch.set(memberRef, memberData)
                // ------------------------
                // Default Rules Post - "system - Community Rules : 1, 2, 3"
                // ------------------------
                val rulesPostData = hashMapOf(
                    "topicID" to topicID,
                    "posterID" to "Ram-Bo",
                    "postTitle" to "Community Rules",
                    "postText" to """
                    1. Be respectful
                    2. Stay on topic
                    3. No harassment
                """.trimIndent(),
                    "likeCount" to 1,
                    "commentCount" to 1,
                    "postedAt" to FieldValue.serverTimestamp(),
                    "isPinned" to true
                )
                batch.set(rulesPostRef, rulesPostData)
                // ------------------------
                // Default Rules Comment Creation - "System - Have fun!"
                // ------------------------
                val rulesCommentData = hashMapOf(
                    "topicID" to topicID,
                    "postID" to postID,
                    "commenterID" to "Ram-Bo",
                    "commentText" to "Have fun!",
                    "parentCommentID" to null,
                    "likeCount" to 1,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                batch.set(rulesCommentRef, rulesCommentData)

                // ------------------------
                // Commit Batch - Write everything in 1 complete batch, or fail the Topic Creation entirely
                // ------------------------
                batch.commit()
                    .addOnSuccessListener {
                        onSuccess(topicID)
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    //TODO:Improve to preserve TopicID for further diving -> Might be needed for post ID and stuff
    //Load Topics from Firestore
    //Reading the URI for loading a topic, after TestParty changes of resId->Uri
//    val uriString = document.getString("imageUri")
//    val imageUri = uriString?.let { Uri.parse(it) }
//    fun getTopics(onResult: (List<Topic>) -> Unit) {
//
//        FirestoreDatabase.topics
//            .get()
//            .addOnSuccessListener { result ->
//
//                val topicsList = mutableListOf<Topic>()
//
//                for (doc in result) {
//
//                    val topicID = doc.getString("topicID") ?: continue
//                    val name = doc.getString("topicName") ?: ""
//                    val desc = doc.getString("topicDesc") ?: ""
//                    val creatorID = doc.getString("creatorID") ?: ""
//                    val memberCount = doc.getLong("memberCount")?.toInt() ?: 0
//                    val imageResId = doc.getLong("imageResId")?.toInt() ?: R.drawable.marquee_logo
//
//                    //I think this could be technically just added to list below as a anonymous topic instead of declaring it here
//                    val topic = Topic(
//                        topicID,
//                        name,
//                        creatorID,
//                        desc,
//                        imageResId,
//                        memberCount
//                    )
//
//                    topicsList.add(topic)
//                }
//
//                onResult(topicsList)
//            }
//    }

    //To hopefully enable keyword searching
    /* If searching by keyword "Chess", will show
        Chess
        Chess Club
        Chess Tournament
     */
    fun searchTopics(
        keyword: String,
        onResult: (List<Model.Topic>) -> Unit
    ) {

        FirestoreDatabase.topics
            .whereGreaterThanOrEqualTo("topicName", keyword)
            .whereLessThanOrEqualTo("topicName", keyword + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->

                val topics = mutableListOf<Model.Topic>()

                for (doc in result) {

                    val topicName = doc.getString("topicName") ?: ""
                    val topicDesc = doc.getString("topicDesc") ?: ""
                    val creatorID = doc.getString("creatorID") ?: ""

                    topics.add(Model.Topic(topicName, creatorID, topicDesc))
                }

                onResult(topics)
            }
    }

    // Allows a User to join a Topic as a member of the Community
    fun joinTopic(
        topicID: String,
        onDone: (Boolean) -> Unit
    ) {

        val userID = FirestoreAuthManager.currentUserId ?: return

        //Set the joining User to a member of Topic & save Timestamp of join date
        val memberData = hashMapOf(
            "role" to "member",
            "joinedAt" to FieldValue.serverTimestamp()
        )

        //Open members subcollection and add new User who is joining
        FirestoreDatabase.topics
            .document(topicID)
            .collection("members")
            .document(userID)
            .set(memberData)
            .addOnSuccessListener {
                //Increment member count of Topic
                FirestoreDatabase.topics
                    .document(topicID)
                    .update("memberCount", FieldValue.increment(1))
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { onDone(false) }
            }
            .addOnFailureListener {
                onDone(false)
            }
    }

    //Allows a User to leave a Topic/Community
    fun leaveTopic(
        topicID: String,
        onDone: (Boolean) -> Unit
    ) {

        val userID = FirestoreAuthManager.currentUserId ?: return

        //Open members subcollection and delete user by UserID in subcollection
        FirestoreDatabase.topics
            .document(topicID)
            .collection("members")
            .document(userID)
            .delete()
            .addOnSuccessListener {

                FirestoreDatabase.topics
                    .document(topicID)
                    .update("memberCount", FieldValue.increment(-1))
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { onDone(false) }
            }
            .addOnFailureListener {
                onDone(false)
            }
    }

    //CommunityBoard functions
    /*
        createEvent()
        getEvents()
        rsvpToEvent()
        unRSVPToEvent()
        //View a User's profile...
        //Send a friend request...

     */

    //Event functions
    /*
        createEvent()
        getEvents()
        rsvpToEvent()
        filtering()? Less important
     */

/*
Create a Topic X
Fetch Topics X
Join/leave a Topic
Load a Topic’s CommunityBoard
Create Posts
Load Posts
Like/Comment on Posts
 */



}
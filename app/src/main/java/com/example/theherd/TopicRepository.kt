package com.example.theherd

import Model.Topic
import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

//Firestore Topic access is done through FirestoreDatabase.topics
object TopicRepository {
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

                //Use Topic Name as Unique Identifier (Only 1 Topic of this name Exists)
                //Topic Name is stored in the document for access purposes for ID (same values)
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
                    "topicName" to topicName.trim(),    //topic Name == topic ID, so this field can be used to retrieve topicID in the document
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

    fun loadTopics(
        onSuccess: (List<Topic>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        // If we allow topics to be archived/deleted, the isArchived boolean doesn't load archived Topics
        db.collection("topics")
            .whereEqualTo("isArchived", false)
            .get()
            .addOnSuccessListener { documents ->
                val topics = mutableListOf<Topic>()

                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("topicName") ?: continue
                    val desc = doc.getString("topicDesc") ?: ""
                    val creator = doc.getString("creatorID") ?: "unknown"
                    val imageUri = doc.getString("imageUri") ?: "default"
                    val memberCt = doc.getLong("memberCount")?.toInt() ?: 0
                    //Constructor to load a Topic from Firestore
//             Topic(String topicID, String topicName, String topicDesc, String imageUriString, int memberCount)
                    val topic = Topic(
                        id, //Topic ID == Document ID
                        name,
                        desc,
                        imageUri,
                        memberCt
                    )
                    topics.add(topic)
                }
                onSuccess(topics)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    //This is "Broken" unless we decide to upgrade our Firebase plan.
    //It works but it doesn't actually upload images--saves them as a string and loads them if found locally on the device
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            println("Uploading image triggered")
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                onFailure(Exception("Cannot open selected image"))
                return
            }

            // Generate a unique path in Firebase Storage
            val filename = "topic_images/${UUID.randomUUID()}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(filename)

            storageRef.putStream(inputStream)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        println("Image uploaded, DOWNLOAD URL: $downloadUrl")
                        onSuccess(downloadUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    println("Image upload failed: ${exception.message}")
                    onFailure(exception)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    //TODO: joinTopic() must update 3 documents in Firestore in a BATCH
    //TODO: WRITE, or else race conditions/write failures can be a problem. FS Docs for joinTopic update below
    /*
    topics/topicID/members/userID
    users/userID/joinedTopics/topicID
    topics/topicID/memberCount
    */
    // Allows a User to join a Topic as a member of the Community
    fun joinTopic(
        topicID: String,
        onDone: (Boolean) -> Unit
    ) {
        val userID = FirestoreAuthManager.currentUserId ?: return

        //Validate user is not already a member here
        //If already joined, call leave topic?
        //if the TopicID is found in User's joinedTopics subcollection -> user is already a member


        //Otherwise, set the joining User to a member of Topic & save Timestamp of join date
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
//    fun leaveTopic(
//        topicID: String,
//        onDone: (Boolean) -> Unit
//    ) {
//
//        val userID = FirestoreAuthManager.currentUserId ?: return
//
//        //Open members subcollection and delete user by UserID in subcollection
//        FirestoreDatabase.topics
//            .document(topicID)
//            .collection("members")
//            .document(userID)
//            .delete()
//            .addOnSuccessListener {
//
//                FirestoreDatabase.topics
//                    .document(topicID)
//                    .update("memberCount", FieldValue.increment(-1))
//                    .addOnSuccessListener { onDone(true) }
//                    .addOnFailureListener { onDone(false) }
//            }
//            .addOnFailureListener {
//                onDone(false)
//            }
//    }

    //Topic Event functions
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
Load a Topic’s CommunityBoard X
Join/leave a Topic
//Event stuff in Topic needs thought

Create Posts
Load Posts
Like Posts
Comment on Posts

Create comments
Load comments
Like comment
 */



}
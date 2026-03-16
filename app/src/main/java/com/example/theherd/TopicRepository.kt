package com.example.theherd

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID

//Firestore Topic access is done through FirestoreDatabase.topics
object TopicRepository {
    private const val USE_FIRESTORE = true  //what does this do?
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() //Call the singleton Firebase db instance



    ///Create the topic document, add creator as the first member, then set memberCount==1
    fun createTopic(
        topicName: String,
        topicDesc: String,
        onDone: (Boolean) -> Unit
    ) {
        //Validate creating User account for later insert as the first member
        val creatorID = auth.currentUser?.uid ?: run {
            onDone(false)
            println("Creator ID fauth failed")
            return
        }
        //Create the Topic UUID... Come back to this... is it an issue that all UUIDs are the same function? Topic Vs User?
        val topicID = UUID.randomUUID().toString()

        val topicData = hashMapOf(
            "topicID" to topicID,          //TopicID
            "topicName" to topicName,       //Name of the Topic
            "topicDesc" to topicDesc,       //Description of the Topic
            "creatorID" to creatorID,   //The ID of the User creating the Topic
            "memberCount" to 1,         //Set member count == 1 for first user on creation
            "createdAt" to FieldValue.serverTimestamp() //Save creation time of Topic
        )
        //Access the Singleton FirestoreDatabase -> Topics Collection, then set its data by topicID
        FirestoreDatabase.topics
            .document(topicID)
            .set(topicData)
            .addOnSuccessListener {

                // Add creator as first member
                val memberData = hashMapOf(
                    "role" to "moderator",
                    "joinedAt" to FieldValue.serverTimestamp()
                )

                FirestoreDatabase.topics
                    .document(topicID)
                    .collection("members")
                    .document(creatorID)
                    .set(memberData)
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { onDone(false) }

            }
            .addOnFailureListener {
                onDone(false)
            }
    }

/*
Create a Topic
Fetch Topics
Join/leave a Topic
Load a Topic’s CommunityBoard
Create Posts
Load Posts
Like/Comment on Posts
 */



}

/*  Firestore architecture:
    -----------------------
User
  └─ participates in Topics

Topic
  └─ contains CommunityBoard
       └─ contains Posts
            └─ contains Comments         */
package com.example.theherd

import Model.Topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import java.util.UUID

//Firestore Topic access is done through FirestoreDatabase.topics
object TopicRepository {
    private const val USE_FIRESTORE = true  //what does this do?
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() //Call the singleton Firebase db instance

    fun initializeTestTopic() {
        createTopic(
            "Firestore",
            "Testing Firestore topic creation via initialization function until GUI allows",
            R.drawable.marquee_logo
        ) { success ->
            println("Initialization result: $success")
        }
    }

    ///Create the topic document, add creator as the first member, then set memberCount==1
    fun createTopic(
        topicName: String,
        topicDesc: String,
        imageResId: Int,
        onDone: (Boolean) -> Unit
    ) {
        //Validate creating User account for later insert as the first member
        val creatorID = auth.currentUser?.uid ?: run {
            onDone(false)
            println("Creator ID fauth failed")
            return
        }
//        Check if topic already exists by name
        FirestoreDatabase.topics
            .whereEqualTo("topicName", topicName)
            .get()
            .addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {
                    // Topic already exists, do not create
                    println("Topic '$topicName' already exists. Skipping creation.")
                    onDone(false)
                    return@addOnSuccessListener
                }

            // Topic Vs User UUID?
            val topicID = UUID.randomUUID().toString()

            val topicData = hashMapOf(
                "topicID" to topicID,          //TopicID
                "topicName" to topicName,       //Name of the Topic
                "topicDesc" to topicDesc,       //Description of the Topic
                "creatorID" to creatorID,   //The ID of the User creating the Topic
                "memberCount" to 1,         //Set member count == 1 for first user on creation
                "imageResId" to imageResId, //Topic Image for Card Display
                "createdAt" to FieldValue.serverTimestamp() //Save creation time of Topic
            )

            //Access the Singleton FirestoreDatabase -> Topics Collection, then set its data by topicID
                //Currently does not create anything other than Members subcollection, needs to do the rest - Posts(CommBoard - Posts has Comments), Events
                //See Firestore Architecture Doc
            FirestoreDatabase.topics
                .document(topicID)
                .set(topicData)
                .addOnSuccessListener {
                    // Add creator as first member and moderator of the Community Board
                    val memberData = hashMapOf(
                        "role" to "moderator",
                        "joinedAt" to FieldValue.serverTimestamp()
                    )
                    FirestoreDatabase.topics
                        .document(topicID)
                        .collection("members")
                        .document(creatorID)
                        .set(memberData)
                        .addOnSuccessListener {
                            println("Topic '$topicName' created successfully!")
                            onDone(true)
                        }
                        .addOnFailureListener {
                            println("Failed to add creator as member")
                            onDone(false)
                        }
                }
                .addOnFailureListener {
                    println("Failed to create topic")
                    onDone(false)
                }
            }
            .addOnFailureListener {
                println("Error checking if topic exists")
                onDone(false)
            }
    }

    //TODO:Improve to preserve TopicID for further diving -> Might be needed for post ID and stuff
    //Load Topics from Firestore
    fun getTopics(onResult: (List<Topic>) -> Unit) {

        FirestoreDatabase.topics
            .get()
            .addOnSuccessListener { result ->

                val topicsList = mutableListOf<Topic>()

                for (doc in result) {

                    val topicID = doc.getString("topicID") ?: continue
                    val name = doc.getString("topicName") ?: ""
                    val desc = doc.getString("topicDesc") ?: ""
                    val creatorID = doc.getString("creatorID") ?: ""
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: 0
                    val imageResId = doc.getLong("imageResId")?.toInt() ?: R.drawable.marquee_logo

                    val topic = Topic(
                        topicID,
                        name,
                        creatorID,
                        desc,
                        imageResId,
                        memberCount
                    )

                    topicsList.add(topic)
                }

                onResult(topicsList)
            }
    }

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

        val userID = auth.currentUser?.uid ?: run {
            onDone(false)
            return
        }

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

        val userID = auth.currentUser?.uid ?: run {
            onDone(false)
            return
        }

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
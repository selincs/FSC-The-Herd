package com.example.theherd

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FriendProfileActivity : AppCompatActivity() {

    //These values are assigned in loadFriendProfile()
    private var firstName: String = ""
    private var lastName: String = ""
    private var username: String = ""
    private var gradYear: String = ""
    private var friendMajor: String = ""
    private var friendBio: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        val friendId = intent.getStringExtra("FRIEND_ID") ?: return
        val isFriend = intent.getBooleanExtra("IS_FRIEND", false)
        val isIncoming = intent.getBooleanExtra("IS_INCOMING", false)
        val isPending = intent.getBooleanExtra("IS_PENDING", false)
        val disabledBtnColor = Color.parseColor("#808080")

        //loadFriendProfile - gets the Friends fName, lName, major, bio, & gradDate
        loadFriendProfile(friendId) //and sets the fields in the friend profile

        //TODO:currently no way to "create statuses" in app, auto create a welcome on signup? "Joined the Herd on $signUpDate?
        //loadStatusPosts - Loads up to 10 status posts ordered by creation date for the Friend Profile
        loadStatusPosts(friendId)

        //TODO: Remove final hardcoded values after hooking them up correctly/adding functionality
//        val allCommunities = listOf("Android Devs", "Soccer Club", "Gaming", "IEEE", "Hackathon", "Math Club", "AI Research", "Coffee Lovers", "Chess")
//        val sharedWithMe = listOf("Android Devs", "Gaming")

        val actionButton = findViewById<Button>(R.id.actionButton)
        val btnBlock = findViewById<Button>(R.id.btnBlockUser)
        val emptyText = findViewById<TextView>(R.id.emptyCommunitiesText)
        val moreText = findViewById<TextView>(R.id.moreCommunitiesText)
        val emptySharedText = findViewById<TextView>(R.id.emptySharedText)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        // button event listeners
        motivationButton.setOnClickListener {
            val intent = Intent(this, MotivationActivity::class.java)
            startActivity(intent)
        }

        friendsButton.setOnClickListener {
            val intent = Intent(this, FriendsListActivity::class.java)
            startActivity(intent)
        }

        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        eventsButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }
        guideButton.setOnClickListener {
            val intent = Intent(this, GuidesActivity::class.java)
            startActivity(intent)
        }
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        //Set the text of the action button
        when {
            isFriend -> {
                actionButton.text = "Message"

                actionButton.setOnClickListener {
                    Toast.makeText(this, "Opening Chat...", Toast.LENGTH_SHORT).show()
                }
            }

            //If request is already incoming from this user, accept their friend request.
            isIncoming -> {
                actionButton.text = "Accept Request"

                actionButton.setOnClickListener {
                    FriendsRepository.acceptFriendRequest(friendId) { success ->
                        if (success) {
                            Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show()
                            actionButton.text = "Friend added!"
                            actionButton.isEnabled = false
                            actionButton.setBackgroundColor(disabledBtnColor)
                        } else {
                            Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // Outgoing request to this user already exists, no incoming request and a pending request
            !isIncoming && /* request exists */ isPending -> {
                actionButton.text = "Cancel Request"

                actionButton.setOnClickListener {
                    FriendsRepository.cancelFriendRequest(friendId) { success ->
                        if (success) {
                            Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
                            actionButton.text = "Request Cancelled"
                            actionButton.isEnabled = false
                            actionButton.setBackgroundColor(disabledBtnColor)
                        } else {
                            Toast.makeText(this, "Failed to cancel request", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            //Else, Send Friend Request (If CommBoard visiting exists, for example.
            else -> {
                actionButton.text = "Send Friend Request"

                actionButton.setOnClickListener {
                    FriendsRepository.sendFriendRequest(
                        username,
                        onSuccess = {
                            Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show()
                            actionButton.text = "Request Sent"
                            actionButton.isEnabled = false
                            actionButton.setBackgroundColor(disabledBtnColor)
                        },
                        onFailure = {
                            Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        val communitiesRecycler = findViewById<RecyclerView>(R.id.communitiesRecycler)
        loadAllCommunities(friendId) { allCommunities ->
            if (allCommunities.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                communitiesRecycler.visibility = View.GONE
            } else {
                val displayList = allCommunities.take(5)

                communitiesRecycler.layoutManager = LinearLayoutManager(this)
                communitiesRecycler.adapter = AskMeAdapter(this, displayList.toMutableList(), false)

                if (allCommunities.size > 5) {
                    moreText.visibility = View.VISIBLE
                    //I used 8 here, but it still only shows 5? Changing this to say -5
                    moreText.text = "and ${allCommunities.size - 5} more communities!"
                }
            }
            println(allCommunities.size)
        }

        val sharedRecycler = findViewById<RecyclerView>(R.id.sharedTopicsRecycler)
        loadSharedCommunities(friendId) { sharedTopics ->
            if (sharedTopics.isEmpty()) {
                emptySharedText.visibility = View.VISIBLE
                sharedRecycler.visibility = View.GONE
            } else {
                emptySharedText.visibility = View.GONE
                sharedRecycler.visibility = View.VISIBLE

                sharedRecycler.layoutManager = LinearLayoutManager(this)
                sharedRecycler.adapter = AskMeAdapter(this, sharedTopics.toMutableList(), false)
            }
        }

        //Firestore block listener - calls Block User function. BlockUser() will :
        //add user to block list, remove friends on both ends, and delete friend requests atomically
        btnBlock.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Block $firstName?")
                .setMessage("You will no longer see each other in the Herd.")
                .setPositiveButton("Block") { _, _ ->

                    BlockListRepository.blockUser(friendId) { success ->
                        if (success) {
                            Toast.makeText(this, "$firstName has been blocked.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to block user.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //TODO: Change status posts to load from Firestore once we have data in Firestore
//        val statusPosts = listOf(
//            StatusPost("Fountain Fest was 10/10 today! 🎡", "3 hours ago"),
//            StatusPost("Looking for a study group for the CS Senior Project.", "Yesterday"),
//            StatusPost("Java recursion is making my brain melt. 🫠", "2 days ago")
//        )
//
//        val statusRecycler = findViewById<RecyclerView>(R.id.statusPostsRecycler)
//        statusRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        statusRecycler.adapter = StatusAdapter(statusPosts)
    }

    //Should this be in FriendsRepo? It does do Firestore getting, but for now make it work.
    //Retrieves the user doc fields for a single friend's profile, except for statuses -> loadStatusPosts
    private fun loadFriendProfile(friendId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(friendId)
            .get()
            .addOnSuccessListener { doc ->

                firstName = doc.getString("firstName") ?: ""
                lastName = doc.getString("lastName") ?: ""
                friendMajor = doc.getString("major") ?: "Unknown"
                friendBio = doc.getString("bio") ?: ""
                gradYear = doc.getString("graduationDate") ?: ""
                val email = doc.getString("email") ?: ""
                username = if (email.contains("@")) {
                    email.substringBefore("@")
                } else {
                    "Ram User"
                }

                findViewById<TextView>(R.id.nameText).text =
                    "$firstName $lastName".trim()

                findViewById<TextView>(R.id.usernameText).text =
                    "@$username".lowercase()

                findViewById<TextView>(R.id.majorText).text = friendMajor
                findViewById<TextView>(R.id.gradYearText).text = "Class of $gradYear"
                findViewById<TextView>(R.id.bioText).text = friendBio
            }
    }

    //TODO: Validate this function once Firestore has status post data. statusPost-> new subcollection, save latest in user doc
    //Loads up to 10 status posts ordered by creation date for the Friend Profile
    private fun loadStatusPosts(friendId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(friendId)
            .collection("statusPosts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->

                val posts: MutableList<Status> = snapshot.documents.map { doc ->
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    Status(content, timestamp)
                }.toMutableList()

                val recycler = findViewById<RecyclerView>(R.id.statusPostsRecycler)
                recycler.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                recycler.adapter = StatusAdapter(posts)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load status posts", Toast.LENGTH_SHORT).show()
            }
    }

    //Formats the status posts createdAt field for readability
    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days day${if (days == 1L) "" else "s"} ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
                sdf.format(timestamp.toDate())
            }
        }
    }

    //allCommunities entry
    private fun loadAllCommunities(friendId: String, onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(friendId)
            .collection("joinedTopics")
            .get()
            .addOnSuccessListener { docs ->
                val topics = docs.map { doc ->
                    doc.id // topicID = topic name
                }
                onResult(topics)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    //shared communities section
    private fun loadSharedCommunities(friendId: String, onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        val currentUserTopics = mutableSetOf<String>()

        // 1: Get current user's topics
        db.collection("users")
            .document(currentUserId)
            .collection("joinedTopics")
            .get()
            .addOnSuccessListener { currentDocs ->

                for (doc in currentDocs) {
                    currentUserTopics.add(doc.id)
                }

                // 2: Get friend's topics
                db.collection("users")
                    .document(friendId)
                    .collection("joinedTopics")
                    .get()
                    .addOnSuccessListener { friendDocs ->

                        val shared = friendDocs.mapNotNull { doc ->
                            val topicId = doc.id
                            if (currentUserTopics.contains(topicId)) topicId else null
                        }

                        onResult(shared)
                    }
                    .addOnFailureListener {
                        onResult(emptyList())
                    }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

}
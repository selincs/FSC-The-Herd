package com.example.theherd

import android.content.Intent
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

        //loadFriendProfile - gets the Friends fName, lName, major, bio, & gradDate
        loadFriendProfile(friendId) //and sets the fields in the friend profile

        //TODO:currently no way to "create statuses" in app, auto create a welcome on signup? "Joined the Herd on $signUpDate?
        //loadStatusPosts - Loads up to 10 status posts ordered by creation date for the Friend Profile
//        loadStatusPosts(friendId)

        //TODO: Fix friend name passed from adapter
        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Ram User"
//        val username = intent.getStringExtra("USERNAME") ?: "@${friendName.replace(" ", "_").lowercase()}"
//        val gradYear = intent.getStringExtra("GRAD_YEAR") ?: "2026"

//
//        val nameParts = friendName.split(" ", limit = 2)
//        val firstName = nameParts.getOrNull(0) ?: ""
//        val lastName = nameParts.getOrNull(1) ?: ""
//
//        val friendMajor = "Computer Science"
//        val friendBio = "Senior at Farmingdale. Love coding in Kotlin and playing soccer!"
        //TODO: Remove final hardcoded values after hooking them up correctly/adding functionality
        val allCommunities = listOf("Android Devs", "Soccer Club", "Gaming", "IEEE", "Hackathon", "Math Club", "AI Research", "Coffee Lovers", "Chess")
        val sharedWithMe = listOf("Android Devs", "Gaming")

//        val tvName = findViewById<TextView>(R.id.nameText)
//        val tvUsername = findViewById<TextView>(R.id.usernameText)
//        val tvMajor = findViewById<TextView>(R.id.majorText)
//        val tvGradYear = findViewById<TextView>(R.id.gradYearText)
//        val tvBio = findViewById<TextView>(R.id.bioText)

        val actionButton = findViewById<Button>(R.id.actionButton)
        val btnBlock = findViewById<Button>(R.id.btnBlockUser)
        val emptyText = findViewById<TextView>(R.id.emptyCommunitiesText)
        val moreText = findViewById<TextView>(R.id.moreCommunitiesText)

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

        guideButton.setOnClickListener {
            val intent = Intent(this, GuidesActivity::class.java)
            startActivity(intent)
        }

//        tvName.text = "$firstName $lastName".trim()
//        tvUsername.text = username
//        tvMajor.text = friendMajor
//        tvGradYear.text = "Class of $gradYear"
//        tvBio.text = friendBio

        //TODO: If theres time, this could do multiple new things.. if friend request is pending, could say "Pending" and block sends.
        if (isFriend) {
            actionButton.text = "Message"
        } else {
            //else if(isFriend && status == "pending") actBtn.text= "Request Pending" -> Then what? Let user cancel? Disable button?
            actionButton.text = "Send Friend Request"
        }

        val communitiesRecycler = findViewById<RecyclerView>(R.id.communitiesRecycler)
        if (allCommunities.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            communitiesRecycler.visibility = View.GONE
        } else {
            val displayList = allCommunities.take(8).toMutableList()
            communitiesRecycler.layoutManager = LinearLayoutManager(this)
            communitiesRecycler.adapter = AskMeAdapter(this, displayList, false)

            if (allCommunities.size > 8) {
                moreText.visibility = View.VISIBLE
                moreText.text = "and ${allCommunities.size - 8} more communities"
            }
        }

        val sharedRecycler = findViewById<RecyclerView>(R.id.sharedTopicsRecycler)
        sharedRecycler.layoutManager = LinearLayoutManager(this)
        sharedRecycler.adapter = AskMeAdapter(this, sharedWithMe.toMutableList(), false)

        actionButton.setOnClickListener {
            if (isFriend) {
                Toast.makeText(this, "Opening Chat...", Toast.LENGTH_SHORT).show()
            } else {
                actionButton.text = "Request Pending"
                actionButton.isEnabled = false
                Toast.makeText(this, "Request sent to $firstName", Toast.LENGTH_SHORT).show()
            }
        }

        //TODO: Figure out block button functionality... Block lists -> What happens here? How to unblock?
        btnBlock.setOnClickListener {
                        AlertDialog.Builder(this)
                .setTitle("Block $firstName?")
                .setMessage("You will no longer see each other in the Herd.")
                .setPositiveButton("Block") { _, _ ->
                    //TODO: REMOVE MOCKFRIENDSREPO REMOVE FRIEND BUTTON HERE -> RemoveFriend in FriendsRepo
                    FriendsRepository.removeFriend(friendId) { success ->
                        if (success) {
                            Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this, "Failed to remove friend", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //TODO: Block the Friend next, not just remove Friend. combine functions in FriendRepo? Create blockFriend()
                    //TODO: that runs remove too? Then only one function is called in the activity for less code clutter
//                    MockFriendsRepo.removeFriendByName(friendName)
                    Toast.makeText(this, "$firstName has been blocked.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()

//            AlertDialog.Builder(this)
//                .setTitle("Block $firstName?")
//                .setMessage("You will no longer see each other in the Herd.")
//                .setPositiveButton("Block") { _, _ ->
//                    MockFriendsRepo.removeFriendByName(friendName)
//                    Toast.makeText(this, "$firstName has been blocked.", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
        }
        val statusPosts = listOf(
            StatusPost("Fountain Fest was 10/10 today! 🎡", "3 hours ago"),
            StatusPost("Looking for a study group for the CS Senior Project.", "Yesterday"),
            StatusPost("Java recursion is making my brain melt. 🫠", "2 days ago")
        )

        val statusRecycler = findViewById<RecyclerView>(R.id.statusPostsRecycler)
        statusRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        statusRecycler.adapter = StatusAdapter(statusPosts)
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
                    email.substringAfter("@")
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

    //Loads up to 10 status posts ordered by creation date for the Friend Profile
    private fun loadStatusPosts(friendId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(friendId)
            .collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->

                val posts = docs.map { doc ->
                    StatusPost(
                        doc.getString("text") ?: "",
                        "Just now"
                    )
                }

                val recycler = findViewById<RecyclerView>(R.id.statusPostsRecycler)
                recycler.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                recycler.adapter = StatusAdapter(posts)
            }
    }
}
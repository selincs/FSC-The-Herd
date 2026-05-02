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

class FriendProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Ram User"
        val username = intent.getStringExtra("USERNAME") ?: "@${friendName.replace(" ", "_").lowercase()}"
        val gradYear = intent.getStringExtra("GRAD_YEAR") ?: "2026"
        val isFriend = intent.getBooleanExtra("IS_FRIEND", false)

        val nameParts = friendName.split(" ", limit = 2)
        val firstName = nameParts.getOrNull(0) ?: ""
        val lastName = nameParts.getOrNull(1) ?: ""

        val friendMajor = "Computer Science"
        val friendBio = "Senior at Farmingdale. Love coding in Kotlin and playing soccer!"
        val allCommunities = listOf("Android Devs", "Soccer Club", "Gaming", "IEEE", "Hackathon", "Math Club", "AI Research", "Coffee Lovers", "Chess")
        val sharedWithMe = listOf("Android Devs", "Gaming")

        val tvName = findViewById<TextView>(R.id.nameText)
        val tvUsername = findViewById<TextView>(R.id.usernameText)
        val tvMajor = findViewById<TextView>(R.id.majorText)
        val tvGradYear = findViewById<TextView>(R.id.gradYearText)
        val tvBio = findViewById<TextView>(R.id.bioText)

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
            finish()
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

        tvName.text = "$firstName $lastName".trim()
        tvUsername.text = username
        tvMajor.text = friendMajor
        tvGradYear.text = "Class of $gradYear"
        tvBio.text = friendBio

        if (isFriend) {
            actionButton.text = "Message"
        } else {
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

        btnBlock.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Block $firstName?")
                .setMessage("You will no longer see each other in the Herd.")
                .setPositiveButton("Block") { _, _ ->
                    val friendToBlock = MockFriendsRepo.getMockFriends().find { it.name == friendName }

                    if (friendToBlock != null) {
                        MockFriendsRepo.blockFriend(friendToBlock)
                        Toast.makeText(this, "$firstName has been blocked.", Toast.LENGTH_SHORT).show()
                    } else {
                        MockFriendsRepo.removeFriendByName(friendName)
                    }
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
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
}
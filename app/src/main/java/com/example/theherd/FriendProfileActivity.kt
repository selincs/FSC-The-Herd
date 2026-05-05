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

class FriendProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        setupNavigation() // sets up all buttons in the tool/nav bar

        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Ram User"
        val friendId = intent.getStringExtra("FRIEND_ID") ?: "Missing Friend ID"
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

                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("FRIEND_NAME", friendName)
                intent.putExtra("FRIEND_ID", friendId)
                intent.putExtra("ONLINE_STATUS", "Online")
                println("Friend Name, frProfAct = $friendName")
                println("Friend ID, frProfAct = $friendId")
                startActivity(intent)
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
                    val blockedUserID = intent.getStringExtra("FRIEND_ID") ?: ""
                    FriendsRepository.blockUser(blockedUserID) { // Argument type mismatch: actual type is 'String?', but 'String' was expected.

                    }
                    /* MockFriendsRepo.removeFriendByName(friendName)
                    Toast.makeText(this, "$firstName has been blocked.", Toast.LENGTH_SHORT).show() */
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
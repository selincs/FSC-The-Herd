package com.example.theherd

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SpecificCommunityActivity : AppCompatActivity() {

    private lateinit var postAdapter: PostAdapter
    private val postsList = ArrayList<Post>()
    private var communityName: String = "General"

    private val startCreatePost = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val title = data?.getStringExtra("POST_TITLE") ?: ""
            val content = data?.getStringExtra("POST_CONTENT") ?: ""
            val author = data?.getStringExtra("POST_AUTHOR") ?: PreferencesManager.getFullName(this)

            val newPost = Post(title, content, author)
            postsList.add(0, newPost)

            postAdapter.notifyItemInserted(0)
            findViewById<RecyclerView>(R.id.posts_recycler_view).scrollToPosition(0)

            PreferencesManager.savePosts(this, communityName, postsList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_community)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

        // event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        motivationButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        friendsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            println("In MainActivity: communityButton onclick listener")
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

        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener { finish() }

        communityName = intent.getStringExtra("COMMUNITY_NAME") ?: "General"
        findViewById<TextView>(R.id.specificCommunityTitle).text = communityName

        postsList.clear()
        val savedPosts = PreferencesManager.loadPosts(this, communityName)
        postsList.addAll(savedPosts)

        if (postsList.isEmpty()) {
            postsList.add(Post("Welcome!", "This is the start of the $communityName board.", "Admin"))
            PreferencesManager.savePosts(this, communityName, postsList)
        }

        val recyclerView: RecyclerView = findViewById(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        postAdapter = PostAdapter(postsList, communityName)
        recyclerView.adapter = postAdapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAddPost).setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("COMMUNITY_NAME", communityName)
            startCreatePost.launch(intent)
        }
        setupJoinLeaveSystem()
    }

    private fun setupJoinLeaveSystem() {
        val btnMembership = findViewById<Button>(R.id.btnJoinCommunity) ?: return
        btnMembership.visibility = View.VISIBLE

        // 1. Define the color
        val deepRed = Color.parseColor("#8B0000")

        // 2. Force the tint mode to SRC_ATOP so it paints OVER any theme defaults
        btnMembership.backgroundTintMode = android.graphics.PorterDuff.Mode.SRC_ATOP

        // 3. Apply the color to the background tint
        btnMembership.backgroundTintList = ColorStateList.valueOf(deepRed)

        // 4. (Optional) If it's a MaterialButton, this ensures the stroke doesn't flicker green
        btnMembership.setBackgroundColor(deepRed)

        val allClubs = PreferencesManager.loadAllCommunities(this)
        val currentClub = allClubs.find { it.name == communityName }

        btnMembership.text = if (currentClub?.isJoined == true) "Leave" else "Join"

        btnMembership.setOnClickListener {
            val clubs = PreferencesManager.loadAllCommunities(this)
            val club = clubs.find { it.name == communityName }

            if (club?.isJoined == true) {
                club.isJoined = false
                btnMembership.text = "Join"
                Toast.makeText(this, "Left $communityName", Toast.LENGTH_SHORT).show()
            } else {
                club?.isJoined = true
                btnMembership.text = "Leave"
                Toast.makeText(this, "Joined $communityName!", Toast.LENGTH_SHORT).show()
            }

            // Re-confirm the red color after the click event finishes
            btnMembership.backgroundTintList = ColorStateList.valueOf(deepRed)

            PreferencesManager.saveAllCommunities(this, clubs)
        }
    }
}
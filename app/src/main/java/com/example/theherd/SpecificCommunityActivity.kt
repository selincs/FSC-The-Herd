package com.example.theherd

import android.content.Intent
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
import Model.Post

class SpecificCommunityActivity : AppCompatActivity() {

    private lateinit var postAdapter: PostAdapter
    private val postsList = ArrayList<Post>()
    private var communityName: String = "General"

    private val startCreatePost = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
           loadPostsFromFirestore()
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
        motivationButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
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

        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener { finish() }

        communityName = intent.getStringExtra("COMMUNITY_NAME") ?: "General"
        //firestore path
        val topicID = intent.getStringExtra("TOPIC_ID") ?: return
        findViewById<TextView>(R.id.specificCommunityTitle).text = communityName



        val recyclerView: RecyclerView = findViewById(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        postAdapter = PostAdapter(postsList, communityName, topicID)
        recyclerView.adapter = postAdapter

        loadPostsFromFirestore()


        findViewById<ExtendedFloatingActionButton>(R.id.fabAddPost).setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("COMMUNITY_NAME", communityName)
            intent.putExtra("TOPIC_ID", topicID)
            startCreatePost.launch(intent)
        }

        setupJoinLeaveSystem()
    }

    private fun setupJoinLeaveSystem() {
        val btnJoin = findViewById<Button>(R.id.btnJoinCommunity)


        val allClubs = PreferencesManager.loadAllCommunities(this)
        val currentClub = allClubs.find { it.name == communityName }

        if (currentClub?.isJoined == true) {

            btnJoin.visibility = View.GONE
        } else {
            btnJoin.visibility = View.VISIBLE

        }

        btnJoin.setOnClickListener {
            val clubs = PreferencesManager.loadAllCommunities(this)
            clubs.find { it.name == communityName }?.isJoined = true
            PreferencesManager.saveAllCommunities(this, clubs)

            btnJoin.visibility = View.GONE

            Toast.makeText(this, "Joined $communityName!", Toast.LENGTH_SHORT).show()
        }




    }
    private fun loadPostsFromFirestore(){
        postsList.clear()

        val topicID = intent.getStringExtra("TOPIC_ID") ?: return

        PostRepository.getPosts(topicID){ posts ->
            postsList.clear()
            postsList.addAll(posts)
            postAdapter.notifyDataSetChanged()

        }
    }
}
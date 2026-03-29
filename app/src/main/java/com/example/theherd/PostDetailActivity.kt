package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class PostDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

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

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

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

        @Suppress("DEPRECATION")
        val post = intent.getSerializableExtra("SELECTED_POST") as? Post

        val communityName = intent.getStringExtra("COMMUNITY_NAME") ?: ""

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        val titleTextView = findViewById<TextView>(R.id.detailTitle)
        val authorTextView = findViewById<TextView>(R.id.detailAuthor)
        val contentTextView = findViewById<TextView>(R.id.detailContent)

        if (post != null) {
            titleTextView.text = post.title
            authorTextView.text = "By: ${post.author}"
            contentTextView.text = post.content

            setupComments(post, communityName)
        }
    }
    private fun setupComments(post: Post, communityName: String) {
        if (post.comments == null) post.comments = ArrayList()

        val displayList = ArrayList<String>()

        post.comments.forEach {
            val timeLabel = formatTimestamp(it.timestamp)
            displayList.add("[$timeLabel] ${it.author}: ${it.text}")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        val listView = findViewById<ListView>(R.id.commentsListView)
        listView.adapter = adapter

        val btnSend = findViewById<ImageButton>(R.id.btnSendComment)
        val commentInput = findViewById<EditText>(R.id.etCommentInput)

        btnSend.setOnClickListener {
            val text = commentInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val currentUserName = PreferencesManager.getFullName(this)

                val newComment = Comment(currentUserName, text)

                post.comments.add(newComment)

                val timeLabel = formatTimestamp(newComment.timestamp)
                displayList.add("[$timeLabel] ${newComment.author}: ${text}")
                adapter.notifyDataSetChanged()

                commentInput.text.clear()
                saveUpdatedPost(post, communityName)
            }
        }
    }

    private fun saveUpdatedPost(updatedPost: Post, communityName: String) {
        val allPosts = PreferencesManager.loadPosts(this, communityName)

        val index = allPosts.indexOfFirst { it.title == updatedPost.title && it.author == updatedPost.author }
        if (index != -1) {
            allPosts[index] = updatedPost
            PreferencesManager.savePosts(this, communityName, allPosts)
        }
    }

    private fun formatTimestamp(timeInMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timeInMillis))
    }
}
package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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

        @Suppress("DEPRECATION")
        // gives detail on real ids,
        val postID = intent.getStringExtra("POST_ID") ?: ""
        val topicID = intent.getStringExtra("TOPIC_ID") ?: ""

        val communityName = intent.getStringExtra("COMMUNITY_NAME") ?: ""

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }



        loadPostDetails(topicID, postID)
        setupComments(topicID, postID)
    }

    private fun loadPostDetails(topicID: String, postID: String){
        FirestoreDatabase
            .topics
            .document(topicID)
            .collection("posts")
            .document(postID)
            .get()
            .addOnSuccessListener { doc ->
                val title = doc.getString("postTitle") ?: ""
                val content = doc.getString("postContents")
                    ?: doc.getString("postText")
                    ?: ""

                val author = doc.getString("displayName")
                    ?: doc.getString("posterID")
                    ?: "Rambo"

                val timestamp = doc.getTimestamp("postDateTime")
                    ?: doc.getTimestamp("postedAt")


                val formattedTime = if (timestamp != null) {
                    val sdf = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
                    sdf.format(timestamp.toDate())
                } else {
                    ""
                }

                findViewById<TextView>(R.id.detailTitle).text = title
                findViewById<TextView>(R.id.detailAuthor).text = "By $author"
                findViewById<TextView>(R.id.detailContent).text = content
                findViewById<TextView>(R.id.detailTime).text = formattedTime
            }

    }
    private fun setupComments(topicID: String, postID: String) {
        val listView = findViewById<ListView>(R.id.commentsListView)
        val commentInput = findViewById<EditText>(R.id.etCommentInput)
        val btnSend = findViewById<ImageButton>(R.id.btnSendComment)

        val displayList = ArrayList<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        //load comments from firestore
        fun loadComments(){
            CommentRepository.getComments(topicID, postID){ comments ->
                displayList.clear()

                for (c in comments){
                    val text = "${c.commentedByUID}: ${c.commContents}"
                    displayList.add(text)
                }
                adapter.notifyDataSetChanged()
            }
        }

        loadComments()

        //send comment
        btnSend.setOnClickListener {
            val text = commentInput.text.toString().trim()

            if(text.isNotEmpty()){
                CommentRepository.createComment(
                    topicID,
                    postID,
                    text
                ){ sucess ->
                    if(sucess){
                        commentInput.text.clear()
                        loadComments() // refresh list.
                    } else{
                        Toast.makeText(this, " failed to send comment", Toast.LENGTH_SHORT ).show()
                    }

                }
            }

        }
    }


}
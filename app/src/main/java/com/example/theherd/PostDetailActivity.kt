package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class PostDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setupNavigation() // sets up all buttons in the tool/nav bar

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

        val commentsList = ArrayList<Model.Comment>()
        lateinit var adapter: CommentAdapter

        fun loadComments() {
            CommentRepository.getComments(topicID, postID) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                adapter.notifyDataSetChanged()
            }
        }

        adapter = CommentAdapter(this, commentsList) { comment ->
            CommentRepository.toggleLikeComment(topicID, postID, comment.commentID) { success ->
                if (success) {
                    loadComments()
                } else {
                    Toast.makeText(this, "Failed to like comment", Toast.LENGTH_SHORT).show()
                }
            }
        }

        listView.adapter = adapter
        loadComments()

        btnSend.setOnClickListener {
            val text = commentInput.text.toString().trim()

            if (text.isNotEmpty()) {
                CommentRepository.createComment(topicID, postID, text) { success ->
                    if (success) {
                        commentInput.text.clear()
                        loadComments()
                    } else {
                        Toast.makeText(this, "failed to send comment", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}
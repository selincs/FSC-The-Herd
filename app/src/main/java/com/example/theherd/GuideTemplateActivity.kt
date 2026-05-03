package com.example.theherd

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Model.Guide

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideTemplateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_template)


        // toolbar buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

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
        friendsButton.setOnClickListener {
            val intent = Intent(this, FriendsListActivity::class.java)
            startActivity(intent)
        }
        motivationButton.setOnClickListener {
            val intent = Intent(this, MotivationActivity::class.java)
            startActivity(intent)
        }



        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        val titleText: TextView = findViewById(R.id.dynamic_guide_title)
        val descText: TextView = findViewById(R.id.dynamic_guide_desc)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { finish() }

        val incomingGuideId = intent.getStringExtra("GUIDE_ID")

        val selectedGuide = getDatabaseGuides().find { it.id == incomingGuideId }


        if (selectedGuide != null) {
            titleText.text = selectedGuide.title
            descText.text = selectedGuide.description
        } else {
            Toast.makeText(this, "Error: Guide not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        val thumbsUpButton: ImageButton = findViewById(R.id.thumbsUpButton)
        val thumbsDownButton: ImageButton = findViewById(R.id.thumbsDownButton)
        val feedbackEditText: TextView = findViewById(R.id.feedbackEditText)
        val layoutFeedback: LinearLayout = findViewById(R.id.layoutFeedback)
        val submitFeedbackButton: Button = findViewById(R.id.submitFeedbackButton)

        thumbsUpButton.setOnClickListener {
            Toast.makeText(this, "Glad it helped", Toast.LENGTH_SHORT).show()
            layoutFeedback.visibility = View.GONE
        }

        thumbsDownButton.setOnClickListener {
            layoutFeedback.visibility = View.VISIBLE
        }

        submitFeedbackButton.setOnClickListener {
            val feedback = feedbackEditText.text.toString()
            if (feedback.isNotBlank()) {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                layoutFeedback.visibility = View.GONE
                feedbackEditText.setText("")
            } else {
                Toast.makeText(this, "Please tell us how to improve!", Toast.LENGTH_SHORT).show()
            }
        }

        val rvQuestions: RecyclerView = findViewById(R.id.rvQuestions)
        rvQuestions.layoutManager = LinearLayoutManager(this)

        val dummyQuestions = listOf(
            mapOf(
                "username" to "John Doe",
                "questionText" to "Does the shuttle run every 15 minutes?",
                "timestamp" to System.currentTimeMillis() - 3600000
            ),
            mapOf(
                "username" to "Jane Smith",
                "questionText" to "Where is the science building?",
                "timestamp" to System.currentTimeMillis() - 7200000
            ),
        )

        val adapter = QuestionsAdapter(dummyQuestions) { question ->
            // do nothing for now
        }
        rvQuestions.adapter = adapter

    }

    private fun getDatabaseGuides(): List<Guide> {
        return listOf(
            Guide("101", "Finding the Hidden Science Lab Classrooms", "This is the full text of the article! To find the hidden labs, you need to go past the main quad, enter the science building through the side door near the greenhouse, and take the service elevator to the basement.", true, false, "Navigation"),
            Guide("102", "FSC Shuttle Bus Schedule", "The shuttle runs every 15 minutes. It stops at the Student Center, the main parking lot, and the dorms. Don't forget your student ID!", false, true, "Travel"),
            Guide("201", "How to Register for OPSTEP Classes", "Log into the portal, click on Academics, and select the OPSTEP registration tool. Make sure you meet with your advisor first to get your registration pin.", true, false, "Academic")
        )
    }
}
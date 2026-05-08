package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideTemplateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_template)

        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

        interestsButton.setOnClickListener {
            startActivity(Intent(this, TopicsActivity::class.java))
        }

        communityButton.setOnClickListener {
            startActivity(Intent(this, CommunityBoardActivity::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        guideButton.setOnClickListener {
            startActivity(Intent(this, GuidesActivity::class.java))
        }

        friendsButton.setOnClickListener {
            startActivity(Intent(this, FriendsListActivity::class.java))
        }

        motivationButton.setOnClickListener {
            startActivity(Intent(this, MotivationActivity::class.java))
        }

        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { finish() }

        val titleText: TextView = findViewById(R.id.dynamic_guide_title)
        val descText: TextView = findViewById(R.id.dynamic_guide_desc)

        val guideId = intent.getStringExtra("GUIDE_ID") ?: ""

        if (guideId.isBlank()) {
            Toast.makeText(this, "Error: Missing guide ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        GuideRepository.getGuideFromFirestoreById(guideId) { selectedGuide ->
            if (selectedGuide != null) {
                titleText.text = selectedGuide.title
                descText.text = selectedGuide.description
            } else {
                Toast.makeText(this, "Error: Guide not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val thumbsUpButton: ImageButton = findViewById(R.id.thumbsUpButton)
        val thumbsDownButton: ImageButton = findViewById(R.id.thumbsDownButton)
        val feedbackEditText: EditText = findViewById(R.id.feedbackEditText)
        val layoutFeedback: LinearLayout = findViewById(R.id.layoutFeedback)
        val submitFeedbackButton: Button = findViewById(R.id.submitFeedbackButton)

        thumbsUpButton.setOnClickListener {
            GuideRepository.voteGuide(guideId, "up") { success ->
                if (success) {
                    Toast.makeText(this, "Glad it helped", Toast.LENGTH_SHORT).show()
                    layoutFeedback.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        thumbsDownButton.setOnClickListener {
            GuideRepository.voteGuide(guideId, "down") { success ->
                if (success) {
                    layoutFeedback.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        submitFeedbackButton.setOnClickListener {
            val feedback = feedbackEditText.text.toString().trim()

            if (feedback.isNotBlank()) {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                layoutFeedback.visibility = View.GONE
                feedbackEditText.text.clear()
            } else {
                Toast.makeText(this, "Please tell us how to improve!", Toast.LENGTH_SHORT).show()
            }
        }

        val rvQuestions: RecyclerView = findViewById(R.id.rvQuestions)
        val etQuestionInput: EditText = findViewById(R.id.etQuestionInput)
        val askQuestionButton: Button = findViewById(R.id.askQuestionButton)

        rvQuestions.layoutManager = LinearLayoutManager(this)

        val adapter = QuestionsAdapter(emptyList()) { question ->
            val intent = Intent(this, AnswerActivity::class.java)

            intent.putExtra("guideId", guideId)
            intent.putExtra("questionId", question.questionId)
            intent.putExtra("questionText", question.questionText)
            intent.putExtra("username", question.username)
            intent.putExtra("timestamp", question.timestamp ?: System.currentTimeMillis())

            startActivity(intent)
        }

        rvQuestions.adapter = adapter

        fun refreshQuestions() {
            GuideRepository.getQuestions(guideId) { questions ->
                adapter.updateData(questions)
            }
        }

        refreshQuestions()

        askQuestionButton.setOnClickListener {
            val questionText = etQuestionInput.text.toString().trim()

            if (questionText.isBlank()) {
                Toast.makeText(this, "Please type a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GuideRepository.addQuestion(
                guideId = guideId,
                questionText = questionText,
                username = "Student"
            ) { success ->
                if (success) {
                    etQuestionInput.text.clear()
                    refreshQuestions()
                    Toast.makeText(this, "Question posted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to post question", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
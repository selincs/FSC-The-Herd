package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideTemplateActivity : BaseActivity() {

    private lateinit var questionsAdapter: QuestionsAdapter
    private var guideId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_template)
        setupNavigation()

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { finish() }

        val titleText: TextView = findViewById(R.id.dynamic_guide_title)
        val descText: TextView = findViewById(R.id.dynamic_guide_desc)

        guideId = intent.getStringExtra("GUIDE_ID") ?: ""

        if (guideId.isBlank()) {
            Toast.makeText(this, "Error: Missing guide ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        GuideRepository.getGuideFromFirestoreById(guideId) { selectedGuide ->
            if (selectedGuide != null) {
                titleText.text = selectedGuide.title
                descText.text = selectedGuide.description
            }
        }

        val rvQuestions: RecyclerView = findViewById(R.id.rvQuestions)
        val etQuestionInput: EditText = findViewById(R.id.etQuestionInput)
        val askQuestionButton: Button = findViewById(R.id.askQuestionButton)

        rvQuestions.layoutManager = LinearLayoutManager(this)

        questionsAdapter = QuestionsAdapter(emptyList()) { question ->
            val intent = Intent(this, AnswerActivity::class.java)
            intent.putExtra("guideId", guideId)
            intent.putExtra("questionId", question.questionId)
            intent.putExtra("questionText", question.questionText)
            intent.putExtra("username", question.username)
            intent.putExtra("timestamp", question.timestamp ?: System.currentTimeMillis())
            startActivity(intent)
        }

        rvQuestions.adapter = questionsAdapter

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

        refreshQuestions()
        setupHelpfulButtons()
    }

    override fun onResume() {
        super.onResume()

        if (::questionsAdapter.isInitialized && guideId.isNotBlank()) {
            refreshQuestions()
        }
    }

    private fun refreshQuestions() {
        GuideRepository.getQuestions(guideId) { questions ->
            questionsAdapter.updateData(questions)
        }
    }

    private fun setupHelpfulButtons() {
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
    }
}
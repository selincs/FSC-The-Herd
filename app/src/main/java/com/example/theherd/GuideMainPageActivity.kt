package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theherd.databinding.ActivityGuideMainBinding
import com.google.firebase.Timestamp

class GuideMainPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideMainBinding
    private lateinit var questionsAdapter: QuestionsAdapter
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Navigation"
        binding.tvCategoryTitle.text = "$categoryName Questions"

        setupBottomNavigation()
        setupQuestionsList()
    }

    private fun setupQuestionsList() {
        questionsAdapter = QuestionsAdapter(emptyList()) { question ->
            val intent = Intent(this, GuideQuestionDetailActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("QUESTION_ID", question["questionID"].toString())
            intent.putExtra("QUESTION_TEXT", question["questionText"].toString())
            startActivity(intent)
        }

        binding.rvQuestions.layoutManager = LinearLayoutManager(this)
        binding.rvQuestions.adapter = questionsAdapter

        // TEMP dummy questions so we can test the answer screen
        val dummyQuestions = listOf(
            mapOf(
                "questionID" to "q1",
                "username" to "student1",
                "questionText" to "Where is the tutoring center?",
                "createdAt" to Timestamp.now()
            ),
            mapOf(
                "questionID" to "q2",
                "username" to "student2",
                "questionText" to "How do I register for classes?",
                "createdAt" to Timestamp.now()
            ),
            mapOf(
                "questionID" to "q3",
                "username" to "student3",
                "questionText" to "Where do I find financial aid help?",
                "createdAt" to Timestamp.now()
            )
        )

        questionsAdapter.updateData(dummyQuestions)
    }

    private fun setupBottomNavigation() {
        findViewById<Button>(R.id.motivation_button).setOnClickListener {
            startActivity(Intent(this, MotivationActivity::class.java))
        }

        findViewById<Button>(R.id.friends_button).setOnClickListener {
            startActivity(Intent(this, FriendsListActivity::class.java))
        }

        findViewById<Button>(R.id.interests_button).setOnClickListener {
            startActivity(Intent(this, TopicsActivity::class.java))
        }

        findViewById<Button>(R.id.community_button).setOnClickListener {
            startActivity(Intent(this, CommunityBoardActivity::class.java))
        }

        findViewById<Button>(R.id.profile_button).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.guide_button).setOnClickListener {
            startActivity(Intent(this, GuidesActivity::class.java))
        }
    }
}
package com.example.theherd

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnswerActivity : BaseActivity() {

    private lateinit var adapter: AnswerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        val guideId = intent.getStringExtra("guideId") ?: ""
        val questionId = intent.getStringExtra("questionId") ?: ""
        val questionText = intent.getStringExtra("questionText") ?: "No question found"
        val username = intent.getStringExtra("username") ?: "Anonymous"

        val tvUser = findViewById<TextView>(R.id.tvAnswerQuestionUser)
        val tvQuestion = findViewById<TextView>(R.id.tvAnswerQuestionText)
        val rvAnswers = findViewById<RecyclerView>(R.id.rvAnswers)

        val etAnswerInput = findViewById<EditText>(R.id.etAnswerInput)
        val btnSubmitAnswer = findViewById<Button>(R.id.btnSubmitAnswer)

        tvUser.text = username
        tvQuestion.text = questionText

        adapter = AnswerAdapter(
            emptyList(),
            onUpVoteClicked = { answer ->
                GuideRepository.voteAnswer(guideId, questionId, answer, "up") { success ->
                    if (success) {
                        loadAnswers(guideId, questionId)
                    } else {
                        Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDownVoteClicked = { answer ->
                GuideRepository.voteAnswer(guideId, questionId, answer, "down") { success ->
                    if (success) {
                        loadAnswers(guideId, questionId)
                    } else {
                        Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        rvAnswers.layoutManager = LinearLayoutManager(this)
        rvAnswers.adapter = adapter

        btnSubmitAnswer.setOnClickListener {
            val answerText = etAnswerInput.text.toString().trim()

            if (answerText.isBlank()) {
                Toast.makeText(this, "Please type an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GuideRepository.addAnswer(
                guideId = guideId,
                questionId = questionId,
                answerText = answerText,
                username = "Student"
            ) { success ->
                if (success) {
                    etAnswerInput.text.clear()
                    loadAnswers(guideId, questionId)
                } else {
                    Toast.makeText(this, "Could not post answer", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadAnswers(guideId, questionId)
    }

    private fun loadAnswers(guideId: String, questionId: String) {
        GuideRepository.getAnswers(guideId, questionId) { answers ->
            adapter.updateData(answers)
        }
    }
}
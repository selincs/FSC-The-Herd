package com.example.theherd

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AnswerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        val questionText = intent.getStringExtra("questionText") ?: "No question found"
        val username = intent.getStringExtra("username") ?: "Anonymous"

        val tvUser = findViewById<TextView>(R.id.tvAnswerQuestionUser)
        val tvQuestion = findViewById<TextView>(R.id.tvAnswerQuestionText)

        tvUser.text = username
        tvQuestion.text = questionText
    }
}
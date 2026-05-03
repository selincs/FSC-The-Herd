package com.example.theherd

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideQuestionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_question_detail)

        val questionText = intent.getStringExtra("QUESTION_TEXT") ?: ""

        val tvQuestion = findViewById<TextView>(R.id.tvQuestion)
        val rvAnswers = findViewById<RecyclerView>(R.id.rvAnswers)

        tvQuestion.text = questionText

        rvAnswers.layoutManager = LinearLayoutManager(this)

        // TEMP dummy answers (we replace with Firestore next)
        val dummyAnswers = mutableListOf(
            Answer("Yes, it runs every 15 minutes"),
            Answer("Check the website for updated schedule")
        )

        rvAnswers.adapter = AnswerAdapter(dummyAnswers)
    }
}
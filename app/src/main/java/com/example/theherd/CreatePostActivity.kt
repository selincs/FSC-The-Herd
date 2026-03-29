package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreatePostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val communityName = intent.getStringExtra("COMMUNITY_NAME") ?: "General"
        val etTitle = findViewById<EditText>(R.id.etPostTitle)
        val etContent = findViewById<EditText>(R.id.etPostContent)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitPost)
        val btnCancel = findViewById<TextView>(R.id.btnCancelPost)

        btnCancel.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            val authorName = PreferencesManager.getFullName(this)

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("POST_TITLE", title)
                resultIntent.putExtra("POST_CONTENT", content)

                resultIntent.putExtra("POST_AUTHOR", authorName)

                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
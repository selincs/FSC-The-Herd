package com.example.theherd


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


        val topicID = intent.getStringExtra("TOPIC_ID") ?: ""
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




                if(title.isNotEmpty() && content.isNotEmpty()){
                    PostRepository.createPost(
                        topicID,
                        title,
                        content
                    ) {
                        success -> if ( success){
                            Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                    } else{
                        Toast.makeText(this, "failed to create post", Toast.LENGTH_SHORT).show()
                    }
                    }

                }else{
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

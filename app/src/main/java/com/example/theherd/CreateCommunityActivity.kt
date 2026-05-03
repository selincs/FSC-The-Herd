package com.example.theherd


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class CreateCommunityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)
        
        val btnCancel: TextView = findViewById(R.id.btnCancelCreate)
        val btnLaunch: Button = findViewById(R.id.launch_community_button)
        val etName: TextInputEditText = findViewById(R.id.community_name)
        val etDesc: TextInputEditText = findViewById(R.id.community_description)

        btnCancel.setOnClickListener {
            finish()
        }

        btnLaunch.setOnClickListener {
            val name = etName.text.toString().trim()
            val description = etDesc.text.toString().trim()

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            } else {
                //get the user ID
                val creatorID = FirestoreAuthManager.currentUserId
                //null check
                if(creatorID == null){
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                //START THE REPPP CALL
                TopicRepository.createTopic(
                    name,
                    description,
                    null,
                    creatorID,
                    {
                        _ ->
                        Toast.makeText(this, "Community Created!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    {
                        exception -> Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                )

            }
        }
    }
}
package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateCommunityActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val launchCommunityButton: Button = findViewById(R.id.launch_community_button)

        // text fields
        val communityNameField: TextInputEditText = findViewById(R.id.community_name)
        val communityDescriptionField: TextInputEditText = findViewById(R.id.community_description)

        // button event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        motivationButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        friendsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        interestsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

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

        launchCommunityButton.setOnClickListener {
            val name = communityNameField.toString()
            var description = communityDescriptionField.toString()
            if (name.isEmpty() || description.isEmpty())
                    Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "Community successfully created!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
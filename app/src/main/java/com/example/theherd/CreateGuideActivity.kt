package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.widget.Toolbar

class CreateGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("in Create Guide Activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_guide)

        // Settings btn
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        settingsButton.setOnClickListener { view ->

            // Creates popup menu connected to settings button
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)

            // Handles menu clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_account_settings -> {

                        val intent = Intent(this, AccountSettingsActivity::class.java)
                        startActivity(intent)

                        true
                    }

                    R.id.menu_logout -> {

                        //When settings btn clicked add a way to logout the user

                        // Goes to LoginActivity and clears back stack
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

        // xml views
        val guideTitle: EditText = findViewById(R.id.enter_guide_title)
        val categories: Spinner = findViewById(R.id.guide_categories)
        val guideContent: EditText = findViewById(R.id.guide_content_field)
        val submitButton: Button = findViewById(R.id.submit_request_button)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        // display dropdown list of categories
        val categoriesList = arrayOf( "Navigation", "Travel", "Academic", "Financial Aid", "Housing", "Clubs", "Health & Wellness", "Other (specify below)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categories.adapter = adapter

        // button event listeners
        eventsButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        motivationButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        friendsButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        interestsButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

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

        submitButton.setOnClickListener {
            val title = guideTitle.text.toString()
            val content = guideContent.text.toString()
            when {
                (title.isEmpty() || content.isEmpty()) -> {
                    Toast.makeText(this, "Error: Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Guide request submitted!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
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
import android.view.View

class CreateGuideActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("in Create Guide Activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_guide)
        setupNavigation() // sets up all buttons in the tool/nav bar

        // Settings btn
        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
//        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
//        settingsButton.setOnClickListener { view ->
//            SettingsMenuHelper.showSettingsMenu(this, view)
//        }
//
//        val backButton = findViewById<ImageButton>(R.id.btnBack)
//        backButton.visibility = View.VISIBLE
//        backButton.setOnClickListener {
//            finish() // Closes this page and goes back
//        }

//        toolbar.setNavigationOnClickListener {
////            finish()
////        }
////
//        homeButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            startActivity(intent)
//        }

        // xml views
        val guideTitle: EditText = findViewById(R.id.enter_guide_title)
        val categories: Spinner = findViewById(R.id.guide_categories)
        val guideContent: EditText = findViewById(R.id.guide_content_field)
        val submitButton: Button = findViewById(R.id.submit_request_button)

        // toolbar
//        val toolbar: Toolbar = findViewById(R.id.topToolbar)
//        val homeButton: ImageButton = findViewById(R.id.homeButton)
//        setSupportActionBar(toolbar)

        // display dropdown list of categories
        val categoriesList = arrayOf( "Navigation", "Travel", "Academic", "Financial Aid", "Housing", "Clubs", "Health & Wellness", "Other (specify below)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categories.adapter = adapter

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

    }
}
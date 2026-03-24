package com.example.theherd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var askMeRecycler: RecyclerView
    private lateinit var adapter: AskMeAdapter
    private val topics = mutableListOf<String>()
    private lateinit var newTopicInput: EditText
    private lateinit var addTopicButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // connects XML

        val nameDisplay: TextView = findViewById(R.id.nameText)
        val editProfileButton: Button = findViewById(R.id.editProfileButton)

        // Initial load from PrefsManager
        nameDisplay.text = PreferencesManager.getFullName(this)

        editProfileButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit Name")

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 20, 60, 20)
            }

            val etFirst = EditText(this).apply { hint = "First Name" }
            val etLast = EditText(this).apply { hint = "Last Name" }

            layout.addView(etFirst)
            layout.addView(etLast)
            builder.setView(layout)

            builder.setPositiveButton("Save") { _, _ ->
                val fName = etFirst.text.toString().trim()
                val lName = etLast.text.toString().trim()

                if (fName.isNotEmpty() && lName.isNotEmpty()) {
                    PreferencesManager.saveFullName(this, fName, lName)
                    nameDisplay.text = "$fName $lName"
                    Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

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
        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
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
        settingsButton.setOnClickListener { view ->

            // Creates popup menu connected to settings btn
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

        toolbar.setNavigationOnClickListener {
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        // Ask Me About RecyclerView
        askMeRecycler = findViewById(R.id.askMeRecycler)
        newTopicInput = findViewById(R.id.newTopicInput)
        addTopicButton = findViewById(R.id.addTopicButton)

        adapter = AskMeAdapter(this, topics)
        askMeRecycler.layoutManager = LinearLayoutManager(this)
        askMeRecycler.adapter = adapter

        // Add new topic
        addTopicButton.setOnClickListener {
            val newTopic = newTopicInput.text.toString().trim()
            if (newTopic.isNotEmpty()) {
                topics.add(newTopic)
                adapter.notifyItemInserted(topics.size - 1)
                askMeRecycler.scrollToPosition(topics.size - 1)
                newTopicInput.text.clear()
            }
        }
    }
}
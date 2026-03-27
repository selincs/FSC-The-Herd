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


import android.widget.*


class ProfileActivity : AppCompatActivity() {


    private lateinit var askMeRecycler: RecyclerView
    private lateinit var adapter: AskMeAdapter
    private val topics = mutableListOf<String>()

    private lateinit var newTopicInput: EditText
    private lateinit var addTopicButton: Button
    private lateinit var editProfileButton: Button

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        editProfileButton = findViewById(R.id.editProfileButton)

        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val majorInput = findViewById<EditText>(R.id.majorInput)
        val gradYearInput = findViewById<EditText>(R.id.gradYearInput)
        val bioInput = findViewById<EditText>(R.id.bioInput)

        val interestsText = findViewById<TextView>(R.id.selectedInterestsText)
        val otherInterestInput = findViewById<EditText>(R.id.otherInterestInput)

        val fitnessCheck = findViewById<CheckBox>(R.id.fitnessCheck)
        val codingCheck = findViewById<CheckBox>(R.id.codingCheck)
        val musicCheck = findViewById<CheckBox>(R.id.musicCheck)
        val gamingCheck = findViewById<CheckBox>(R.id.gamingCheck)
        val artCheck = findViewById<CheckBox>(R.id.artCheck)

        // Load saved data

        usernameInput.setText(PreferencesManager.getUsername(this))
        gradYearInput.setText(PreferencesManager.getGradYear(this))
        bioInput.setText(PreferencesManager.getBio(this))

        val fullName = PreferencesManager.getFullName(this)
        val parts = fullName.split(" ")

        if (parts.isNotEmpty()) firstNameInput.setText(parts[0])
        if (parts.size > 1) lastNameInput.setText(parts[1])

        bioInput.setText(PreferencesManager.getBio(this))

        val savedInterests = PreferencesManager.getInterests(this)
        interestsText.text = savedInterests.joinToString(", ")
        // OPTIONAL: put "other" back into input if it's not a default one
        val defaultInterests = listOf("Fitness", "Coding", "Music", "Gaming", "Art")

        val customInterest = savedInterests.find { it !in defaultInterests }
        if (customInterest != null) {
            otherInterestInput.setText(customInterest)
        }

        askMeRecycler = findViewById(R.id.askMeRecycler)
        newTopicInput = findViewById(R.id.newTopicInput)
        addTopicButton = findViewById(R.id.addTopicButton)

        adapter = AskMeAdapter(this, topics, false)
        askMeRecycler.layoutManager = LinearLayoutManager(this)
        askMeRecycler.adapter = adapter

        addTopicButton.setOnClickListener {
            if (isEditing) {
                val topic = newTopicInput.text.toString().trim()
                if (topic.isNotEmpty()) {
                    topics.add(topic)
                    adapter.notifyItemInserted(topics.size - 1)
                    newTopicInput.text.clear()
                }
            }
        }

        editProfileButton.setOnClickListener {
            isEditing = !isEditing

            if (isEditing) {
                setEditMode(true)
                adapter.setEditMode(true)
                editProfileButton.text = "Save Changes"
            } else {
                saveAll(
                    firstNameInput,
                    lastNameInput,
                    usernameInput,
                    majorInput,
                    gradYearInput,
                    bioInput,
                    fitnessCheck,
                    codingCheck,
                    musicCheck,
                    gamingCheck,
                    artCheck,
                    otherInterestInput,
                    interestsText
                )
                setEditMode(false)
                adapter.setEditMode(false)
                editProfileButton.text = "Edit Profile"
            }
        }

        setEditMode(false)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

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

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setEditMode(isEditing: Boolean) {

        val inputs = listOf(
            R.id.firstNameInput,
            R.id.lastNameInput,
            R.id.usernameInput,
            R.id.majorInput,
            R.id.gradYearInput,
            R.id.bioInput,
            R.id.newTopicInput,
            R.id.otherInterestInput
        )

        inputs.forEach {
            val et = findViewById<EditText>(it)
            et.isEnabled = isEditing
            et.isFocusable = isEditing
            et.isFocusableInTouchMode = isEditing
            et.isCursorVisible = isEditing
        }

        val checkboxes = listOf(
            R.id.fitnessCheck,
            R.id.codingCheck,
            R.id.musicCheck,
            R.id.gamingCheck,
            R.id.artCheck
        )

        checkboxes.forEach {
            findViewById<CheckBox>(it).isEnabled = isEditing
        }

        findViewById<Button>(R.id.addTopicButton).visibility =
            if (isEditing) Button.VISIBLE else Button.GONE
    }

    private fun saveAll(
        firstNameInput: EditText,
        lastNameInput: EditText,
        usernameInput: EditText,
        majorInput: EditText,
        gradYearInput: EditText,
        bioInput: EditText,
        fitnessCheck: CheckBox,
        codingCheck: CheckBox,
        musicCheck: CheckBox,
        gamingCheck: CheckBox,
        artCheck: CheckBox,
        otherInterestInput: EditText,
        interestsText: TextView
    ) {

        val fName = firstNameInput.text.toString().trim()
        val lName = lastNameInput.text.toString().trim()
        PreferencesManager.saveUsername(this, usernameInput.text.toString().trim())
        PreferencesManager.saveGradYear(this, gradYearInput.text.toString().trim())

        if (fName.isNotEmpty() && lName.isNotEmpty()) {
            PreferencesManager.saveFullName(this, fName, lName)
        }

        PreferencesManager.saveBio(this, bioInput.text.toString().trim())

        val interests = mutableListOf<String>()

        if (fitnessCheck.isChecked) interests.add("Fitness")
        if (codingCheck.isChecked) interests.add("Coding")
        if (musicCheck.isChecked) interests.add("Music")
        if (gamingCheck.isChecked) interests.add("Gaming")
        if (artCheck.isChecked) interests.add("Art")

        //Other interests
        val otherInterest = otherInterestInput.text.toString().trim()
        if (otherInterest.isNotEmpty()) {
            interests.add(otherInterest)
        }

        PreferencesManager.saveInterests(this, interests)
        interestsText.text = interests.joinToString(", ")

        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
    }
}
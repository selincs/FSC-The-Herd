package com.example.theherd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar


import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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

        val graduationDateInput = findViewById<EditText>(R.id.graduationDateInput)
        val bioInput = findViewById<EditText>(R.id.bioInput)

        //This will need to be changed to autopop once Topic Firestore is done.
        val interestsText = findViewById<TextView>(R.id.selectedInterestsText)
        val otherInterestInput = findViewById<EditText>(R.id.otherInterestInput)

        //Hard coded values
        val fitnessCheck = findViewById<CheckBox>(R.id.fitnessCheck)
        val codingCheck = findViewById<CheckBox>(R.id.codingCheck)
        val musicCheck = findViewById<CheckBox>(R.id.musicCheck)
        val gamingCheck = findViewById<CheckBox>(R.id.gamingCheck)
        val artCheck = findViewById<CheckBox>(R.id.artCheck)

        // Load saved data
        usernameInput.setText(PreferencesManager.getUsername(this))
        graduationDateInput.setText(PreferencesManager.getGradYear(this))
        bioInput.setText(PreferencesManager.getBio(this))
//        usernameInput.setText()

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
                    graduationDateInput,
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

        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
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
            R.id.graduationDateInput,
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
        graduationDateInput: EditText,
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
        PreferencesManager.saveGradYear(this, graduationDateInput.text.toString().trim())

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

    //When Ready, load the whole User Profile from Firestore
    private fun loadProfileFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) return@addOnSuccessListener

                findViewById<EditText>(R.id.firstNameInput)
                    .setText(document.getString("firstName") ?: "")

                findViewById<EditText>(R.id.lastNameInput)
                    .setText(document.getString("lastName") ?: "")

                //Username is FSC Email without @farmingdale.edu
                val email = document.getString("email") ?: ""
                val username = email.substringBefore("@")
//                usernameInput.setText(username)

                //Idk if this works
                findViewById<EditText>(R.id.usernameInput)
                    .setText(document.getString(username) ?: "")



                //Major Input .. Do we ask at Sign Up? Might make things annoying/complicated, maybe just profile
                //Still will need to be stored in Firestore, but maybe starts as null or Major so User sees where
                //to input it

//                findViewById<EditText>(R.id.majorInput)
//                    .setText(document.getString("major") ?: "")

                //graduationDate field in Firestore
                findViewById<EditText>(R.id.graduationDateInput)
                    .setText(document.getString("graduationDate") ?: "")

                findViewById<EditText>(R.id.bioInput)
                    .setText(document.getString("bio") ?: "")

                val interests = document.get("interests") as? List<String> ?: emptyList()

                findViewById<TextView>(R.id.selectedInterestsText)
                    .text = interests.joinToString(", ")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_LONG).show()
            }
    }
}
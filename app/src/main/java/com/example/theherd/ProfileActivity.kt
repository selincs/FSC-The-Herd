package com.example.theherd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.View


import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ProfileActivity : AppCompatActivity() {
    private lateinit var communitiesRecycler: RecyclerView
    private val communities = mutableListOf<String>()
    private lateinit var statusPostsRecycler: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private val statusPosts = mutableListOf<Status>()
    private lateinit var askMeRecycler: RecyclerView
    private lateinit var adapter: AskMeAdapter
    private val topics = mutableListOf<String>()

    private lateinit var newTopicInput: EditText
    private lateinit var addTopicButton: Button
    private lateinit var editProfileButton: Button
    private var defaultFieldBackground: Drawable? = null    //Default highlight color

    private var isEditing = false

    private val PICK_IMAGE_REQUEST = 1

    //Loads the User's profile from Firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        statusPostsRecycler = findViewById(R.id.statusPostsRecycler)

        statusPostsRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        statusPostsRecycler.addItemDecoration(SpacingItemDecoration(24, true))

        statusAdapter = StatusAdapter(statusPosts)
        statusPostsRecycler.adapter = statusAdapter

        loadStatusPostsFromFirestore()


        communitiesRecycler = findViewById(R.id.communitiesRecycler)
        communitiesRecycler.layoutManager = LinearLayoutManager(this)
        communitiesRecycler.adapter = AskMeAdapter(this, communities, false)

        loadCommunitiesFromFirestore()

        editProfileButton = findViewById(R.id.editProfileButton)

        val profileImage = findViewById<ImageView>(R.id.profileImage)

        profileImage.setOnClickListener {
            if (isEditing) {
                showAvatarPicker()
            } else {
                Toast.makeText(this, "Tap Edit Profile to change avatar", Toast.LENGTH_SHORT).show()
            }
        }

        //Declare GUI elements as Vals
        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val majorInput = findViewById<EditText>(R.id.majorInput)

        val graduationDateInput = findViewById<EditText>(R.id.graduationDateInput)
        val bioInput = findViewById<EditText>(R.id.bioInput)

        //Disable the Profile fields a User cannot directly change
        lastNameInput.isFocusable = false //Last name probably cant change without some verification
        lastNameInput.isClickable = false
        usernameInput.isFocusable = false //Username = Email? Decide at some point
        usernameInput.isClickable = false
        graduationDateInput.isFocusable = false //Graduation Date editable once in acc settings
        graduationDateInput.isClickable = false

        //This will need to be changed to autopop once Topic Firestore is done.
        //val interestsText = findViewById<TextView>(R.id.selectedInterestsText)
        val otherInterestInput = findViewById<EditText>(R.id.otherInterestInput)

        /*//Hard coded values
        val fitnessCheck = findViewById<CheckBox>(R.id.fitnessCheck)
        val codingCheck = findViewById<CheckBox>(R.id.codingCheck)
        val musicCheck = findViewById<CheckBox>(R.id.musicCheck)
        val gamingCheck = findViewById<CheckBox>(R.id.gamingCheck)
        val artCheck = findViewById<CheckBox>(R.id.artCheck)
*/
        // Load saved data from Firestore into Profile
        loadProfileFromFirestore()

        //Interests not pop from Firestore yet, use default
        val savedInterests = PreferencesManager.getInterests(this)
        //interestsText.text = savedInterests.joinToString(", ")
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

        //Listener for editing profile
        editProfileButton.setOnClickListener {
            isEditing = !isEditing

            if (isEditing) {
                setEditMode(true)
                editProfileButton.text = "Save Changes"
            } else {
                saveAll(
                    firstNameInput,
                    majorInput,
                    bioInput,
                )
                setEditMode(false)
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

        // Toolbar + Listeners
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        // button event listeners
        eventsButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }

        motivationButton.setOnClickListener {
            val intent = Intent(this, MotivationActivity::class.java)
            startActivity(intent)
        }

        friendsButton.setOnClickListener {
            val intent = Intent(this, FriendsListActivity::class.java)
            startActivity(intent)
        }

        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
            startActivity(intent)
        }
        communityButton.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
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

    private fun showAvatarPicker() {
        val avatars = arrayOf(
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5
        )

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Choose an Avatar")

        builder.setItems(arrayOf("Default", "Avatar 1", "Avatar 2", "Avatar 3", "Avatar 4")) { _, which ->
            val selectedAvatar = avatars[which]

            val profileImage = findViewById<ImageView>(R.id.profileImage)
            profileImage.setImageResource(selectedAvatar)

            saveAvatarToFirestore("avatar_${which + 1}")
        }

        builder.show()
    }

    private fun saveAvatarToFirestore(avatarName: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("avatar", avatarName)
    }

    private fun loadCommunitiesFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("joinedTopics")
            .get()
            .addOnSuccessListener { snapshot ->

                communities.clear()

                for (doc in snapshot.documents) {
                    val name = doc.id
                    communities.add(name)
                }

                if (communities.isEmpty()) {
                    Toast.makeText(this, "No communities yet", Toast.LENGTH_SHORT).show()
                }

                (communitiesRecycler.adapter as AskMeAdapter).notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load communities", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadStatusPostsFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("statusPosts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val newPosts: List<Status> = snapshot.documents.map { doc ->
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    Status(content, timestamp)
                }

                println("Status posts success in ProfileAct - loadStatusPostsfromFS")
                statusAdapter.updateData(newPosts)

                // handle empty state
                if (newPosts.isEmpty()) {
                    println("Status posts is empty in ProfileAct - loadStatusPostsfromFS")
                    Toast.makeText(this, "No statuses yet", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load status posts", Toast.LENGTH_SHORT).show()
                println("Error loading statuses: ${e.message}")
            }
    }

    private fun setEditMode(isEditing: Boolean) {

        val inputs = listOf(
            R.id.firstNameInput,
            R.id.majorInput,
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
            //Set editable fields to highlight color
            if (isEditing) {
                et.setBackgroundResource(R.drawable.edit_field_background)
            } else {
                et.background = defaultFieldBackground
            }
        }

        findViewById<Button>(R.id.addTopicButton).visibility =
            if (isEditing) Button.VISIBLE else Button.GONE
    }

    private fun saveAll(
        firstNameInput: EditText,
        majorInput: EditText,
        bioInput: EditText,
    ) {
        //Validate user via Fauth mgr
        val user = FirestoreAuthManager.auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = user.uid

        val firstName = firstNameInput.text.toString().trim()
        val major = majorInput.text.toString().trim()
        val bio = bioInput.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "major" to major,
            "bio" to bio
        )
        FirestoreDatabase.users
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                println("Profile updated in FS")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            val profileImage = findViewById<ImageView>(R.id.profileImage)
            profileImage.setImageURI(imageUri)

            // TODO: upload to Firebase Storage here
        }
    }

    //Loads the Firestore profile via FirebaseAuth of current logged-in User - Can this move to userRepository?
    private fun loadProfileFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()
        val avatarName = "default"

        val avatarResId = when (avatarName) {
            "default" -> R.drawable.avatar_1
            "avatar_1" -> R.drawable.avatar_2
            "avatar_2" -> R.drawable.avatar_3
            "avatar_3" -> R.drawable.avatar_4
            "avatar_4" -> R.drawable.avatar_5
            else -> R.drawable.avatar_1
        }


        findViewById<ImageView>(R.id.profileImage).setImageResource(avatarResId)

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
                findViewById<EditText>(R.id.usernameInput)
                    .setText(username)

                //Major starts as unlisted, can be updated by user in profile
                findViewById<EditText>(R.id.majorInput)
                    .setText(document.getString("major") ?: "Add your Major here!")

                //graduationDate field in Firestore
                findViewById<EditText>(R.id.graduationDateInput)
                    .setText(document.getString("graduationDate") ?: "")

                findViewById<EditText>(R.id.bioInput)
                    .setText(document.getString("bio") ?: "")

//                val interests = document.get("interests") as? List<String> ?: emptyList()

                //Implement interests after other fields populate
//                findViewById<TextView>(R.id.selectedInterestsText)
//                    .text = interests.joinToString(", ")

                println("Profile loaded for user " + FirestoreAuthManager.currentUserId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_LONG).show()
            }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}
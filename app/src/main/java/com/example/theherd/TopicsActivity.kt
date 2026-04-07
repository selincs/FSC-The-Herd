package com.example.theherd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import Model.Topic
import android.app.AlertDialog
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

import android.app.Activity
import android.net.Uri



class TopicsActivity : AppCompatActivity() {

    private lateinit var adapter: TopicsAdapter
    private lateinit var topicsList: MutableList<Topic>

    private var selectedImageUri: Uri? = null  // Stores the uploaded image
    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics)

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

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample topics - Hardcoded
        topicsList = mutableListOf(
            Topic("Gym Buddies", "user123", "Connect with fellow gym goers", R.drawable.gym.toString()),
            Topic("Chess Club", "user456", "Join the strategy fun!", R.drawable.chess.toString()),
            Topic("Hiking Lovers", "user789", "Explore trails together", R.drawable.hiking.toString()),
            Topic("Foodies", "user321", "Share recipes and restaurants", R.drawable.food.toString())
        )


        //Adapter with sample topics
        adapter = TopicsAdapter(topicsList)
        recyclerView.adapter = adapter

        // Append Firestore loaded topics to topicsList with sample topics
        //Verify placement of loadTopicsFromFS()in code->Merging happened since this was put here
//        loadTopicsFromFirestore(topicsList) Broken after Test Party Merge, revisit fnc later anyway

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //Enter Community Button in Topics Activity - Not the one in the NavBar
        val enterCommunityBtn = findViewById<Button>(R.id.communityBoardButton)

        enterCommunityBtn.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            startActivity(intent)
        }

        //Create Button starts here
        val createButton = findViewById<Button>(R.id.createTopicButton)

        createButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_create_topic, null)

            val nameInput = dialogView.findViewById<EditText>(R.id.inputTopicName)
            val descInput = dialogView.findViewById<EditText>(R.id.inputTopicDesc)
            val uploadButton = dialogView.findViewById<ImageButton>(R.id.uploadImageButton)

            // Reset selected image for each dialog open
            selectedImageUri = null

            // Upload button opens device image picker
            uploadButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_CODE)
            }

            AlertDialog.Builder(this)
                .setTitle("Create New Topic")
                .setView(dialogView)
                .setPositiveButton("Create") { _, _ ->

                    val name = nameInput.text.toString()
                    val desc = descInput.text.toString()
                    //THIS IS FOR CREATING A NEW TOPIC
                    if (name.isNotEmpty()) {
                        val newTopic = if (selectedImageUri != null) {
                            // Store the URI as a string for your Topic model
                            Topic(name, "currentUser", desc, selectedImageUri.toString())
                        }
                        else {
                            // Set imageUriString to default image if none selected
                            println("Default image option, no image selected")
                            Topic(name, "currentUser", desc, "default")
                        }
                        //If User is null, stop topic creation and return
                        val userID = SessionManager.getUser()?.userID ?: return@setPositiveButton

                     //Firestore createTopic() fnc call
                        TopicRepository.createTopic(
                            name,
                            desc,
                            selectedImageUri,
                            userID,
                            onSuccess = { topicID ->
                                //Set imageURI to either use selected image or default image
                                val imageUriString = selectedImageUri?.toString() ?: "default"
                                val newTopic = Topic(
                                    name,
                                    userID,
                                    desc,
                                    imageUriString
                                )

                                topicsList.add(newTopic)
                                adapter.updateList(topicsList)
                                //Update the Topics List with a new Topic at the bottom
//                                adapter.notifyItemInserted(topicsList.size - 1) //Doesn't work
                                Toast.makeText(this, "Topic created!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                Toast.makeText(this,exception.message ?: "Failed to create topic",Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(this, "Topic name cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


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

        toolbar.setNavigationOnClickListener {
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun loadTopicsFromFirestore(list: MutableList<Topic>) {

        FirestoreDatabase.db.collection("topics")
            .get()
            .addOnSuccessListener { result ->

                for (doc in result) {

                    val topicID = doc.getString("topicID") ?: continue
                    val topicName = doc.getString("topicName") ?: ""
                    val topicDesc = doc.getString("topicDesc") ?: ""
                    val creatorID = doc.getString("creatorID") ?: ""
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: 0
                    val imageResId = doc.getLong("imageResId")?.toInt() ?: R.drawable.marquee_logo


                    // Create Topic object (using your constructor)
                    val topic = Topic(topicName, creatorID, topicDesc, imageResId)

                    topic.setMemberCount(memberCount)

                    // Add to list
                    list.add(topic)
                }

                // RecyclerView doesnt auto refresh, use notifyDataSetChanged() to update
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                println("Failed to load topics from Firestore")
            }
    }

    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
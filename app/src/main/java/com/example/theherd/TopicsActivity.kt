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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import android.view.View

class TopicsActivity : AppCompatActivity() {

    private lateinit var adapter: TopicsAdapter
    private lateinit var topicsList: MutableList<Topic>

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

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mutable list so we can add Firestore data later
//        val topicsList = mutableListOf<Topic>() dont use local var, moved to data members of class

        // Sample topics
        topicsList = mutableListOf(
            Topic("Gym Buddies", "user123", "Connect with fellow gym goers", R.drawable.gym),
            Topic("Chess Club", "user456", "Join the strategy fun!", R.drawable.chess),
            Topic("Hiking Lovers", "user789", "Explore trails together", R.drawable.hiking),
            Topic("Foodies", "user321", "Share recipes and restaurants", R.drawable.food)
        )

        //Adapter with sample topics
        adapter = TopicsAdapter(topicsList)
        recyclerView.adapter = adapter

        // Append Firestore loaded topics to topicsList with sample topics
//        loadTopicsFromFirestore(topicsList) Broken after Test Party Merge, revisit fnc later anyway

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val createButton = findViewById<Button>(R.id.createTopicButton)

        createButton.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.dialog_create_topic, null)

            val nameInput = dialogView.findViewById<EditText>(R.id.inputTopicName)
            val descInput = dialogView.findViewById<EditText>(R.id.inputTopicDesc)
            val spinner = dialogView.findViewById<Spinner>(R.id.imageSpinner)

            // Image options
            val imageOptions = listOf("Gym", "Chess", "Hiking", "Food")
            val imageMap = mapOf(
                "Gym" to R.drawable.gym,
                "Chess" to R.drawable.chess,
                "Hiking" to R.drawable.hiking,
                "Food" to R.drawable.food
            )

            val adapterSpinner = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                imageOptions)

            spinner.adapter = adapterSpinner

            AlertDialog.Builder(this)
                .setTitle("Create New Topic")
                .setView(dialogView)
                .setPositiveButton("Create") { _, _ ->

                    val name = nameInput.text.toString()
                    val desc = descInput.text.toString()
                    val selectedImage = imageMap[spinner.selectedItem.toString()] ?: R.drawable.gym

                    if (name.isNotEmpty()) {
                        val newTopic = Topic(name, "currentUser", desc, selectedImage)

                        topicsList.add(newTopic)
                        adapter.updateList(topicsList)
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
}
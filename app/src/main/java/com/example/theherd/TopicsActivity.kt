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
import android.view.View

import android.app.Activity
import android.net.Uri
import android.widget.TextView


class TopicsActivity : BaseActivity() {

    private lateinit var adapter: TopicsAdapter
    private lateinit var topicsList: MutableList<Topic>
    private var showingJoinedOnly = false
    private var joinedTopicIDs: MutableSet<String> = mutableSetOf()

    private var selectedImageUri: Uri? = null  // Stores the uploaded image URI

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics)
        setupNavigation() // sets up all buttons in the tool/nav bar

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample topics - Hardcoded - Make Real Topics out of these later before removing
//        topicsList = mutableListOf(
//            Topic("Gym Buddies", "user123", "Connect with fellow gym goers", R.drawable.gym.toString()),
//            Topic("Chess Club", "user456", "Join the strategy fun!", R.drawable.chess.toString()),
//            Topic("Hiking Lovers", "user789", "Explore trails together", R.drawable.hiking.toString()),
//            Topic("Foodies", "user321", "Share recipes and restaurants", R.drawable.food.toString())
//        )
//        topicsList = mutableListOf()
//
//        //Adapter with sample topics
//        adapter = TopicsAdapter(topicsList)
//        recyclerView.adapter = adapter
//
//        //Call loadTopics helper in TopicsActivity to load Firestore Topics
//        loadTopics()
        //Get the list of topics a user has joined from Firestore as joinedIDs
        TopicRepository.getUserJoinedTopicIDs(
            onSuccess = { joinedIDs ->

                joinedTopicIDs =
                    joinedIDs.toMutableSet() //store the joinedTopicIDs to query Firestore only once

                TopicRepository.loadTopics( //Load all topics from Firestore into topicsList next
                    onSuccess = { loadedTopics ->
                        //Store the list of loaded Topics
                        topicsList = loadedTopics.toMutableList()

                        adapter = TopicsAdapter(    //Pass both lists to the Adapter
                            topicsList,
                            joinedTopicIDs
                        )
                        recyclerView.adapter = adapter

                    },
                    onFailure = {
                        println("Failed to load topics: ${it.message}")
                    }
                )
            },
            onFailure = {
                println("Failed to load the user's joined topics: ${it.message}")
            }
        )

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //Enter Community Button in Topics Activity - Not the one in the NavBar
        val myCommunitiesBtn = findViewById<Button>(R.id.myCommunitiesButton)
        val viewTopicsHint = findViewById<TextView>(R.id.viewTopicsHint)

        //showingJoinedOnly == false on program start
        myCommunitiesBtn.setOnClickListener {
            //Set button state for joined topics / all topics
            showingJoinedOnly = !showingJoinedOnly

            if (showingJoinedOnly) {
                // Show ONLY joined topics
                val filtered = topicsList.filter { topic ->
                    joinedTopicIDs.contains(topic.topicID)
                }

                adapter.updateList(filtered)
                viewTopicsHint.text = "Want to view all Topics?"
                myCommunitiesBtn.text = "Show All Topics"
                Toast.makeText(this, "Showing your communities", Toast.LENGTH_SHORT).show()

            } else { // Show ALL topics - Default program state
                adapter.updateList(topicsList)
                viewTopicsHint.text = "Want to view topics you've joined?"
                myCommunitiesBtn.text = "My Communities"
                Toast.makeText(this, "Showing all topics", Toast.LENGTH_SHORT).show()
            }
        }

        //Create Topic Button
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
                println("UPLOAD BUTTON PRESSED")
            }

            AlertDialog.Builder(this)
                .setTitle("Create New Topic")
                .setView(dialogView)
                .setPositiveButton("Create") { _, _ ->

                    val name = nameInput.text.toString()
                    val desc = descInput.text.toString()
                    //THIS IS FOR CREATING A NEW TOPIC
                    if (name.isEmpty()) {
                        //Currently if name is empty, dialog closes
                        Toast.makeText(this, "Topic name cannot be empty.", Toast.LENGTH_SHORT)
                            .show()
                        return@setPositiveButton
                    }
                    // Only upload the image if the user picked one using TopicRepository uploadImage()
                    if (selectedImageUri != null) {
                        println("Uploading image: $selectedImageUri")
                        TopicRepository.uploadImage(
                            this, // pass activity context to uploadImage in TopicRepository
                            selectedImageUri!!,
                            onSuccess = { downloadUrl ->
                                //Create the Topic and open its details page
                                createTopicInFirestore(name, desc, downloadUrl)
                            },
                            onFailure = { exception ->
                                Toast.makeText(this, "Image upload (local)", Toast.LENGTH_SHORT)
                                    .show()
                                //Create the Topic and open its details page
                                createTopicInFirestore(
                                    name,
                                    desc,
                                    selectedImageUri!!.toString()
                                ) // Store locally
                            }
                        )
                    } else {
                        // No image selected -> use default
                        println("No image selected, using default, Topic created in FS")
                        //Create the Topic and open its details page
                        createTopicInFirestore(name, desc, "default")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    //Topic currently does not update on createTopic()
    //Helper function to create a topic in Firestore via TopicRepository, then creates the new topic and updates the list
    private fun createTopicInFirestore(name: String, desc: String, imageUrl: String) {
        val userID = SessionManager.currentUserId ?: return
        println("userID=" + userID)

        // passes the Firebase URL as a Uri string to TopicRepository function
        TopicRepository.createTopic(
            name, desc, Uri.parse(imageUrl), userID,
            onSuccess = { topicID ->
                val newTopic = Topic(name, userID, desc, imageUrl)
                newTopic.isJoined = true

                Toast.makeText(this, "Topic created!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, TopicDetailActivity::class.java)
                //TODO: Review, should I just pass the firestore fields here as a extra in the intent?
                intent.putExtra("topicID", topicID)
                intent.putExtra("topicName", name)
                intent.putExtra("topicDesc", desc)
                intent.putExtra("memberCount", 1)
                intent.putExtra("isJoined", true)

                startActivity(intent)
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    exception.message ?: "Error: Failed to create topic",
                    Toast.LENGTH_LONG
                ).show()
                println("HERE")
            }
        )
    }

    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        println("Image picker onActivityResult triggered")
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Persist permission so it works after restart
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                selectedImageUri = uri
                println("Persisted permission for URI: $uri")
                println("ImagePicker - selectedImageUri value: $selectedImageUri")
            }
            if (selectedImageUri != null) {
                Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //If the page is navigated back to via the back btn, reload the user's joined topics incase they change elsewhere(TopicDetails)
    override fun onResume() {
        super.onResume()
        TopicRepository.getUserJoinedTopicIDs(
            onSuccess = { joinedIDs ->

                joinedTopicIDs = joinedIDs.toMutableSet() //store the joinedTopicIDs to query Firestore only once

                TopicRepository.loadTopics( //Load all topics from Firestore into topicsList next
                    onSuccess = { loadedTopics  ->
                        //Store the list of loaded Topics
                        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)

                        topicsList = loadedTopics.toMutableList()

                        adapter = TopicsAdapter(    //Pass both lists to the Adapter
                            topicsList,
                            joinedTopicIDs
                        )
                        recyclerView.adapter = adapter

                    },
                    onFailure = {
                        println("Failed to load topics: ${it.message}")
                    }
                )
            },
            onFailure = {
                println("Failed to load the user's joined topics: ${it.message}")
            }
        )
    }
}
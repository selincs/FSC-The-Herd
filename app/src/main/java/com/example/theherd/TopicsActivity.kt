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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.ImageButton
import android.widget.Toast



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

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample topics
        topicsList = mutableListOf(
            Topic("Gym Buddies", "user123", "Connect with fellow gym goers", R.drawable.gym),
            Topic("Chess Club", "user456", "Join the strategy fun!", R.drawable.chess),
            Topic("Hiking Lovers", "user789", "Explore trails together", R.drawable.hiking),
            Topic("Foodies", "user321", "Share recipes and restaurants", R.drawable.food)
        )

        adapter = TopicsAdapter(topicsList)
        recyclerView.adapter = adapter

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

                    if (name.isNotEmpty()) {
                        val newTopic = if (selectedImageUri != null) {
                            // Store the URI as a string for your Topic model
                            Topic(name, "currentUser", desc, selectedImageUri.toString())
                        } else {
                            // Default image if none selected
                            Topic(name, "currentUser", desc, R.drawable.fsclogo)
                        }

                        topicsList.add(newTopic)
                        adapter.updateList(topicsList)
                        Toast.makeText(this, "Topic created!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Topic name cannot be empty.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
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
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

class TopicsActivity : AppCompatActivity() {

    private lateinit var adapter: TopicsAdapter
    private lateinit var topicsList: MutableList<Topic>

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
    }
}
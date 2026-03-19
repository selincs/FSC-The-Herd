package com.example.theherd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import Model.Topic

class TopicsActivity : AppCompatActivity() {

    private lateinit var adapter: TopicsAdapter
    private lateinit var topicsList: List<Topic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics)

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample topics
        topicsList = listOf(
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
    }
}
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
//    private lateinit var topicsList: List<Topic>
    private val topicsList = mutableListOf<Topic>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics)

        val recyclerView = findViewById<RecyclerView>(R.id.topicsRecyclerView)
        val searchBar = findViewById<EditText>(R.id.searchTopics)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mutable list so we can add Firestore data later
//        val topicsList = mutableListOf<Topic>() dont use local var, moved to data members of class

        // Sample topics
        topicsList.addAll(
            listOf(
                Topic("Gym Buddies", "user123", "Connect with fellow gym goers", R.drawable.gym),
                Topic("Chess Club", "user456", "Join the strategy fun!", R.drawable.chess),
                Topic("Hiking Lovers", "user789", "Explore trails together", R.drawable.hiking),
                Topic("Foodies", "user321", "Share recipes and restaurants", R.drawable.food)
            )
        )

        //Adapter with sample topics
        adapter = TopicsAdapter(topicsList)
        recyclerView.adapter = adapter

        // Append Firestore loaded topics to topicsList with sample topics
        loadTopicsFromFirestore(topicsList)

        // Search filter
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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


                    // 🔹 Create Topic object (using your constructor)
                    val topic = Topic(topicName, creatorID, topicDesc, imageResId)

                    topic.setMemberCount(memberCount)

                    // 🔹 Add to list
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
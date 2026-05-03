package com.example.theherd
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager

class EventsActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: EventAdapter
    private var currentEvents = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        recycler = findViewById(R.id.eventsRecycler)

        val topicID = intent.getStringExtra("topicID") ?: return

        val events = EventRepository.getEventsForTopic(topicID)

        // flatten list
        currentEvents.clear()
        currentEvents.addAll(events)

        adapter = EventAdapter(
            currentEvents,

            onEdit = { item ->

                val (date, event) = item

                val input = EditText(this)
                input.setText(event)

                AlertDialog.Builder(this)
                    .setTitle("Edit Event")
                    .setMessage("Date: $date")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newText = input.text.toString()

                        if (newText.isNotBlank()) {

                            EventRepository.updateEvent(topicID, date, event, newText)

                            val index = currentEvents.indexOf(item)
                            if (index != -1) {
                                currentEvents[index] = date to newText
                                adapter.notifyItemChanged(index)
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },

            onRsvp = { event ->
                Toast.makeText(this, "RSVP'd to: $event", Toast.LENGTH_SHORT).show()
            },

            onSend = { event ->
                Toast.makeText(this, "Sent: $event", Toast.LENGTH_SHORT).show()
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ----------------------------
        // NAV BAR BUTTONS + TOOLBAR
        // ----------------------------

        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)

        setSupportActionBar(toolbar)

        interestsButton.setOnClickListener {
            startActivity(Intent(this, TopicsActivity::class.java))
        }

        communityButton.setOnClickListener {
            startActivity(Intent(this, CommunityBoardActivity::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        guideButton.setOnClickListener {
            startActivity(Intent(this, GuidesActivity::class.java))
        }

        friendsButton.setOnClickListener {
            startActivity(Intent(this, FriendsListActivity::class.java))
        }

        motivationButton.setOnClickListener {
            startActivity(Intent(this, MotivationActivity::class.java))
        }

        eventsButton.setOnClickListener {
            startActivity(
                Intent(this, EventsActivity::class.java)
                    .putExtra("topicID", topicID)
            )
        }

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

    private fun formatDate(raw: String): String {
        val parts = raw.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val day = parts[2]

        val monthName = java.time.Month.of(month)
            .name.lowercase()
            .replaceFirstChar { it.uppercase() }

        return "$monthName $day, $year"
    }
}
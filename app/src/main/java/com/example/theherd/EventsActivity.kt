package com.example.theherd
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager

class EventsActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var currentEvents = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        recycler = findViewById(R.id.eventsRecycler)

        eventAdapter = EventAdapter(
            currentEvents,

            onEdit = { event ->

                val input = EditText(this)
                input.setText(event.name)

                AlertDialog.Builder(this)
                    .setTitle("Edit Event")
                    .setMessage("Date: ${event.date}")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = input.text.toString().trim()

                        if (newName.isNotBlank()) {
                            updateEventName(event.topicId, event, newName)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },

            onRsvp = { event ->
                handleRsvp(event)
            },

            onSend = { event ->
                Toast.makeText(this, "Sent: ${event.name}", Toast.LENGTH_SHORT).show()
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = eventAdapter

        loadEvents()

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
            startActivity(Intent(this, EventsActivity::class.java))
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

        findViewById<Button>(R.id.exploreTopicsBtn).setOnClickListener {
            startActivity(Intent(this, TopicsActivity::class.java))
        }
    }

    //Helper fncs below
    private fun loadEvents() {
        val userId = SessionManager.requireUserId()

        UserRepository.getUserEvents(userId) { refs ->

            currentEvents.clear()

            if (refs.isEmpty()) {
                showEmptyState()
                return@getUserEvents
            }

            var remaining = refs.size

            for ((topicId, eventId) in refs) {

                EventRepository.getSingleEvent(
                    topicId,
                    eventId,
                    onSuccess = { event ->
                        currentEvents.add(event)

                        remaining--
                        if (remaining == 0) {
                            sortEvents()
                            println("Events size: ${currentEvents.size}")

                            if (currentEvents.isEmpty()) {
                                showEmptyState()
                            } else {
                                showList()
                            }

                            eventAdapter.notifyDataSetChanged()
                        }
                    },
                    onFailure = {
                        remaining--

                        if (remaining == 0) {
                            if (currentEvents.isEmpty()) {
                                showEmptyState()
                            } else {
                                showList()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun sortEvents() {
        currentEvents.sortBy { "${it.date} ${it.time}" }
    }

    private fun showEmptyState() {
        recycler.visibility = View.GONE
        findViewById<View>(R.id.emptyView).visibility = View.VISIBLE
        findViewById<View>(R.id.upcomingHeader).visibility = View.GONE
    }

    private fun showList() {
        recycler.visibility = View.VISIBLE
        findViewById<View>(R.id.emptyView).visibility = View.GONE
        findViewById<View>(R.id.upcomingHeader).visibility = View.VISIBLE
    }

    private fun updateEventName(topicId: String, event: Event, newName: String) {
        val oldName = event.name

        event.name = newName
        eventAdapter.notifyDataSetChanged()

        EventRepository.updateEventName(
            topicId,
            event.id,
            newName
        ) { success ->
            if (!success) {
                event.name = oldName
                eventAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleRsvp(event: Event) {
        val userId = SessionManager.requireUserId()

        val alreadyRsvpd = event.rsvpUserIds.contains(userId)

        // Ensure topicId is set
        val topicId = event.topicId

        if (topicId.isBlank()) {
            Toast.makeText(this, "Error: Missing topic ID", Toast.LENGTH_SHORT).show()
            return
        }

        if (alreadyRsvpd) {
            // ----------------------------
            // UN-RSVP
            // ----------------------------
            event.rsvpUserIds.remove(userId)
            event.rsvpCount -= 1
            eventAdapter.notifyDataSetChanged()

            EventRepository.updateRsvp(
                topicId,
                event.id,
                event.rsvpUserIds,
                event.rsvpCount
            ) { success ->
                if (!success) {
                    // rollback
                    event.rsvpUserIds.add(userId)
                    event.rsvpCount += 1
                    eventAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Failed to un-RSVP", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this, "UN-RSVP'd to: ${event.name}", Toast.LENGTH_SHORT).show()
            }

            UserRepository.removeUserEvent(userId, event.id)

        } else {
            // ----------------------------
            // RSVP
            // ----------------------------
            event.rsvpUserIds.add(userId)
            event.rsvpCount += 1
            eventAdapter.notifyDataSetChanged()

            EventRepository.updateRsvp(
                topicId,
                event.id,
                event.rsvpUserIds,
                event.rsvpCount
            ) { success ->
                if (!success) {
                    // rollback
                    event.rsvpUserIds.remove(userId)
                    event.rsvpCount -= 1
                    eventAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Failed to RSVP", Toast.LENGTH_SHORT).show()
                }

                Toast.makeText(this, "RSVP'd to: ${event.name}", Toast.LENGTH_SHORT).show()
            }

            UserRepository.addUserEvent(userId, event)
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
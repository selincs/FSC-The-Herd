package com.example.theherd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.GridView
import java.time.LocalDate
import java.time.YearMonth
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopicDetailActivity : AppCompatActivity() {
    private lateinit var eventsRecycler: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var currentEvents = mutableListOf<Event>()
    private val eventsMap = mutableMapOf<String, MutableList<Event>>()

    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var grid: GridView
    private lateinit var monthTitle: TextView

    private var selectedDay: Int? = null
    private var isJoined = false
    private var memberCount: Int = 0
    private var days = mutableListOf<String>()

    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        val topicID = intent.getStringExtra("topicID") ?: return
        loadEventsFromFirestore(topicID)

        // ----------------------------
        // JOIN STATE
        // ----------------------------
        val joinButton = findViewById<Button>(R.id.joinButton)
        isJoined = intent.getBooleanExtra("isJoined", false)
        updateJoinUI(joinButton, isJoined)

        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    val name = document.getString("topicName") ?: ""
                    val desc = document.getString("topicDesc") ?: ""
                    memberCount = document.getLong("memberCount")?.toInt() ?: 0
                    val timestamp = document.getTimestamp("createdAt")

                    val formattedDate = if (timestamp != null) {
                        val sdf = java.text.SimpleDateFormat(
                            "MMM dd, yyyy hh:mm a",
                            java.util.Locale.getDefault()
                        )
                        sdf.format(timestamp.toDate())
                    } else "Unknown"

                    findViewById<TextView>(R.id.detailTopicName).text = name
                    findViewById<TextView>(R.id.detailDescription).text = desc
                    findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"
                    findViewById<TextView>(R.id.detailCreatedAt).text = formattedDate
                }
            }

        // ----------------------------
        // RECYCLER VIEW (EVENTS)
        // ----------------------------
        eventsRecycler = findViewById(R.id.eventsRecycler)

        eventAdapter = EventAdapter(
            currentEvents,
            onEdit = { event ->

                val input = android.widget.EditText(this)
                input.setText(event.name)

                android.app.AlertDialog.Builder(this)
                    .setTitle("Edit Event Name")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = input.text.toString().trim()

                        if (newName.isNotBlank()) {
                            updateEventName(event, newName)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },

            onRsvp = { event ->
                handleRsvp(topicID, event)
                Toast.makeText(this, "RSVP'd to: ${event.name}", Toast.LENGTH_SHORT).show()
            },

            onSend = { event ->
                Toast.makeText(this, "Sent: ${event.name}", Toast.LENGTH_SHORT).show()
            }
        )

        eventsRecycler.layoutManager = LinearLayoutManager(this)
        eventsRecycler.adapter = eventAdapter

        // ----------------------------
        // CALENDAR SETUP
        // ----------------------------
        grid = findViewById(R.id.calendarGrid)
        monthTitle = findViewById(R.id.monthTitle)

        findViewById<ImageButton>(R.id.prevMonthBtn).setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendar()
        }

        findViewById<ImageButton>(R.id.nextMonthBtn).setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendar()
        }

        findViewById<ImageButton>(R.id.addEventBtn).setOnClickListener {
            if (selectedDay == null) {
                Toast.makeText(this, "Select a day first", Toast.LENGTH_SHORT).show()
            } else {
                showEventDialog(selectedDay!!)
            }
        }

        grid.setOnItemClickListener { _, _, position, _ ->

            val dayStr = days[position]

            if (dayStr.isNotEmpty()) {

                selectedDay = dayStr.toInt()
                selectedPosition = position

                // update calendar highlight
                val adapter = grid.adapter as CalendarAdapter
                adapter.setSelectedPosition(position)

                updateUpcomingEvents()

                Toast.makeText(this, "Selected day: $selectedDay", Toast.LENGTH_SHORT).show()
            }
        }

        updateCalendar()
        updateUpcomingEvents()

        // ----------------------------
        // JOIN BUTTON
        // ----------------------------
        joinButton.setOnClickListener {
            if (!isJoined) {
                TopicRepository.joinTopic(topicID) { success ->
                    if (success) {
                        isJoined = true
                        memberCount++
                        findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"
                        updateJoinUI(joinButton, isJoined)
                    }
                }
            } else {
                TopicRepository.leaveTopic(topicID) { success ->
                    if (success) {
                        isJoined = false
                        memberCount--
                        findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"
                        updateJoinUI(joinButton, isJoined)
                    }

                }
            }
        }
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

        val enterCommunityBtn = findViewById<Button>(R.id.communityBoardButton)

        enterCommunityBtn.setOnClickListener {
            startActivity(Intent(this, CommunityBoardActivity::class.java))
        }

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
    }

    // ----------------------------
    // RECYCLER UPDATE LOGIC
    // ----------------------------
    private fun updateUpcomingEvents() {
        val day = selectedDay ?: run {
            currentEvents.clear()
            eventAdapter.notifyDataSetChanged()
            return
        }

        val key = getDateKey(day)
        val events = eventsMap[key]

        currentEvents.clear()

        if (!events.isNullOrEmpty()) {
            currentEvents.addAll(events)
        }

        eventAdapter.notifyDataSetChanged()
    }

    // ----------------------------
    // EVENT STORAGE
    // ----------------------------
    private fun saveEvent(day: Int, name: String, location: String, time: String) {
        val key = getDateKey(day)
        val topicID = intent.getStringExtra("topicID") ?: return

        if (!eventsMap.containsKey(key)) {
            eventsMap[key] = mutableListOf()
        }

        val event = Event(
            name = name,
            location = location,
            time = time,
            hostId = SessionManager.requireUserId(),
            date = key,
            rsvpCount = 0,
            topicId = topicID
        )

        eventsMap[key]?.add(event)
        updateUpcomingEvents()

        EventRepository.createEvent(topicID, key, event) { success ->
            if (!success) {
                Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show()
            }
        }
    }
//    private fun saveEvent(day: Int, name: String) {
//        val key = getDateKey(day)
//        val topicID = intent.getStringExtra("topicID") ?: return
//
//        if (!eventsMap.containsKey(key)) {
//            eventsMap[key] = mutableListOf()
//        }
//
//        val event = Event(
//            name = name,
//            location = "",
//            time = "",
//            hostId = SessionManager.requireUserId(),
//            date = key,
//            rsvpCount = 0,
//            topicId = topicID
//        )
//
//        eventsMap[key]?.add(event)
//        updateUpcomingEvents()
//
//        EventRepository.createEvent(topicID, key, event) { success ->
//            if (!success) {
//                Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun updateEventName(event: Event, newName: String) {
        val topicId = intent.getStringExtra("topicID") ?: return

        val oldName = event.name

        // UI update
        event.name = newName
        updateUpcomingEvents()

        // Firestore update
        EventRepository.updateEventName(
            topicId,
            event.id,
            newName
        ) { success ->
            if (!success) {
                event.name = oldName
                updateUpcomingEvents()

                Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------
    // DATE KEY
    // ----------------------------
    private fun getDateKey(day: Int): String {
        return "${currentMonth.year}-${currentMonth.monthValue}-%02d".format(day)
    }

    // ----------------------------
    // JOIN UI
    // ----------------------------
    private fun updateJoinUI(button: Button, isJoined: Boolean) {
        if (isJoined) {
            button.text = "Leave"
            button.setBackgroundColor(android.graphics.Color.GRAY)
        } else {
            button.text = "Join"
            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
        }

        button.setTextColor(android.graphics.Color.WHITE)
    }

    private fun updateCalendar() {

        days.clear()

        val firstDay = currentMonth.atDay(1)
        val totalDays = currentMonth.lengthOfMonth()

        monthTitle.text =
            "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}"

        val startOffset = firstDay.dayOfWeek.value % 7

        for (i in 0 until startOffset) {
            days.add("")
        }

        for (day in 1..totalDays) {
            days.add(day.toString())
        }

        val adapter = CalendarAdapter(this, days, eventsMap, currentMonth)
        grid.adapter = adapter

        // restore selection after month change
        if (selectedPosition != -1) {
            adapter.setSelectedPosition(selectedPosition)
        }
    }

    private fun showEventDialog(day: Int) {
        val view = layoutInflater.inflate(R.layout.dialog_create_event, null)

        val eventNameInput = view.findViewById<EditText>(R.id.etEventName)
        val locationInput = view.findViewById<EditText>(R.id.etEventLocation)
        val timeInput = view.findViewById<EditText>(R.id.etEventTime)

        // Time picker
        timeInput.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()

            val timePicker = android.app.TimePickerDialog(
                this,
                { _, hour, minute ->
                    val formatted = String.format("%02d:%02d", hour, minute)
                    timeInput.setText(formatted)
                },
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE),
                false
            )
            timePicker.show()
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Create Event on $day")
            .setView(view)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = eventNameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val time = timeInput.text.toString().trim()

            if (name.isEmpty()) {
                eventNameInput.error = "Event name required"
                return@setOnClickListener
            }

            saveEvent(day, name, location, time)

            dialog.dismiss()
        }
    }

    private fun loadEventsFromFirestore(topicId: String) {
        EventRepository.getEventsForTopic(
            topicId,
            onSuccess = { eventPairs ->

                eventsMap.clear()

                for ((dateKey, event) in eventPairs) {
                    if (!eventsMap.containsKey(dateKey)) {
                        eventsMap[dateKey] = mutableListOf()
                    }
                    eventsMap[dateKey]?.add(event)
                }

                updateCalendar()
                updateUpcomingEvents()
            },
            onFailure = {
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleRsvp(topicId: String, event: Event) {
        val userId = SessionManager.requireUserId()

        val alreadyRsvpd = event.rsvpUserIds.contains(userId)
        event.topicId = topicId

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
            }

            UserRepository.addUserEvent(userId, event)
        }
    }
}
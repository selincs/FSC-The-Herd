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
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopicDetailActivity : AppCompatActivity() {
    private lateinit var eventsRecycler: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var currentEvents = mutableListOf<Pair<String, String>>()

    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var grid: GridView
    private lateinit var monthTitle: TextView

    private val eventsMap = mutableMapOf<String, MutableList<String>>()

    private var selectedDay: Int? = null
    private var isJoined = false
    private var memberCount: Int = 0
    private var days = mutableListOf<String>()

    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        val topicID = intent.getStringExtra("topicID") ?: return

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
                input.setText(event.second)

                android.app.AlertDialog.Builder(this)
                    .setTitle("Edit Event")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newText = input.text.toString()
                        if (newText.isNotBlank()) {

                            val (date, oldName) = event
                            updateEvent(date, oldName, newText)
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
            currentEvents.addAll(
                events.map { event ->
                    key to event
                }
            )
        }

        eventAdapter.notifyDataSetChanged()
    }

    // ----------------------------
    // EVENT STORAGE
    // ----------------------------
    private fun saveEvent(day: Int, event: String) {
        val key = getDateKey(day)

        if (!eventsMap.containsKey(key)) {
            eventsMap[key] = mutableListOf()
        }

        eventsMap[key]?.add(event)

        val topicID = intent.getStringExtra("topicID") ?: return
        EventRepository.addEvent(topicID, key, event)

        updateUpcomingEvents()
    }

    private fun updateEvent(date: String, oldValue: String, newValue: String) {
        val key = getDateKey(selectedDay ?: return)

        val list = eventsMap[key] ?: return
        val index = list.indexOf(oldValue)

        if (index != -1) {
            list[index] = newValue
            updateUpcomingEvents()
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

        val editText = android.widget.EditText(this)
        editText.hint = "Enter event name"

        android.app.AlertDialog.Builder(this)
            .setTitle("Create Event on $day")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val eventText = editText.text.toString()

                if (eventText.isNotEmpty()) {
                    saveEvent(day, eventText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
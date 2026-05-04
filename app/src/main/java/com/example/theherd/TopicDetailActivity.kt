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

class TopicDetailActivity : AppCompatActivity() {
    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var grid: GridView
    private lateinit var monthTitle: TextView

    private val eventsMap = mutableMapOf<String, MutableList<String>>()

    private var selectedDay: Int? = null

    private var isJoined = false
    private var memberCount: Int = 0

    private var days = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        // Event TextViews
        val event1 = findViewById<TextView>(R.id.event1)
        val event2 = findViewById<TextView>(R.id.event2)
        val event3 = findViewById<TextView>(R.id.event3)

// Buttons
        val edit1 = findViewById<ImageButton>(R.id.editEvent1)
        val rsvp1 = findViewById<ImageButton>(R.id.rsvpEvent1)
        val send1 = findViewById<ImageButton>(R.id.sendEvent1)

        val edit2 = findViewById<ImageButton>(R.id.editEvent2)
        val rsvp2 = findViewById<ImageButton>(R.id.rsvpEvent2)
        val send2 = findViewById<ImageButton>(R.id.sendEvent2)

        val edit3 = findViewById<ImageButton>(R.id.editEvent3)
        val rsvp3 = findViewById<ImageButton>(R.id.rsvpEvent3)
        val send3 = findViewById<ImageButton>(R.id.sendEvent3)

// Hook listeners ONCE
        setupEventButtons(edit1, rsvp1, send1) { event1.text.toString() }
        setupEventButtons(edit2, rsvp2, send2) { event2.text.toString() }
        setupEventButtons(edit3, rsvp3, send3) { event3.text.toString() }

        val addEventBtn = findViewById<ImageButton>(R.id.addEventBtn)

        val joinButton = findViewById<Button>(R.id.joinButton)
//        val name = intent.getStringExtra("topicName")
//        val desc = intent.getStringExtra("topicDesc")
//        val members = intent.getIntExtra("memberCount", 0)

        val topicID = intent.getStringExtra("topicID") ?: return

        //Use the isJoined status from the query in TopicsAdapter, else default==false
        isJoined = intent.getBooleanExtra("isJoined", false)
        updateJoinUI(joinButton, isJoined)

        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    //If document exists for this topicID, load the needed fields from Firestore
                    val name = document.getString("topicName") ?: ""
                    val desc = document.getString("topicDesc") ?: ""
                    memberCount = document.getLong("memberCount")?.toInt() ?: 0
                    val timestamp = document.getTimestamp("createdAt")

                    // Convert Firestore timestamp into a readable string for creation date field
                    val formattedDate = if (timestamp != null) {
                        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault())
                        sdf.format(timestamp.toDate())
                    } else {
                        "Unknown"
                    }

                    // Update TopicDetails GUI
                    findViewById<TextView>(R.id.detailTopicName).text = name
                    findViewById<TextView>(R.id.detailDescription).text = desc
                    findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"
                    findViewById<TextView>(R.id.detailCreatedAt).text = formattedDate
                    println("TopicDetails success in listener, Document FOUND: ${document.id}")
                    //join button GUI updates called in the listener for the button
                }
            }
            .addOnFailureListener {
                println("TopicDetailsActivity listener failure for $topicID")
                Toast.makeText(this, "Failed to load topic", Toast.LENGTH_SHORT).show()
            }

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

        addEventBtn.setOnClickListener {
            if (selectedDay == null) {
                Toast.makeText(this, "Select a day first", Toast.LENGTH_SHORT).show()
            } else {
                showEventDialog(selectedDay!!)
            }
        }

        updateCalendar()
        updateUpcomingEvents()

        grid.setOnItemClickListener { _, _, position, _ ->
            val dayStr = days[position]

            if (dayStr.isNotEmpty()) {
                val day = dayStr.toInt()
                selectedDay = day

                val adapter = grid.adapter as CalendarAdapter
                adapter.setSelectedPosition(position)

                updateUpcomingEvents()

                Toast.makeText(this, "Selected day: $day", Toast.LENGTH_SHORT).show()
            }
        }

        //join button logic, initial state set near top of onCreate
        joinButton.setOnClickListener {
                //on join button click
                if (!isJoined) {   //&& user is !joined on this Topic, call join Topic in Firestore to add them
                    TopicRepository.joinTopic(topicID) { success ->
                        //If joinTopic==success, set isJoined==true and change button UI state, increment memberCt
                        if (success) {
                            //need to update member count probably
                            isJoined = true
                            memberCount += 1    //increment and update member count
                            findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"

                            updateJoinUI(joinButton, isJoined)
                            Toast.makeText(this, "You joined $topicID!", Toast.LENGTH_SHORT).show()
                        } else { //If join fails for some reason
                            Toast.makeText(this, "Failed to join", Toast.LENGTH_SHORT).show()
                            println("join failed in Join btn press on success")
                        }
                    }
                    //If user has already joined this Topic, call leaveTopic in Firestore
                } else {    //set isJoined = false,
                    TopicRepository.leaveTopic(topicID) { success ->
                        if (success) {  //If user leaves topic successfully, set isJoined==false and update button UI
                            //decrement memberCt
                            isJoined = false
                            memberCount -= 1    //decrement and update member count
                            findViewById<TextView>(R.id.detailMembers).text = "$memberCount members"

                            updateJoinUI(joinButton, isJoined)
                            Toast.makeText(this, "You left $topicID!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to leave", Toast.LENGTH_SHORT).show()
                            println("leave failed in Join btn press on success")
                        }
                    }
                }
        }



        //Enter Community Button in Topics Activity - Not the one in the NavBar
        val enterCommunityBtn = findViewById<Button>(R.id.communityBoardButton)

        enterCommunityBtn.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            startActivity(intent)
        }
    }
    private fun updateCalendar() {

        days.clear()

        val firstDay = currentMonth.atDay(1)
        val totalDays = currentMonth.lengthOfMonth()

        // Month title
        monthTitle.text =
            "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}"

        // correct weekday offset (Sun = 0)
        val startOffset = firstDay.dayOfWeek.value % 7

        for (i in 0 until startOffset) {
            days.add("")
        }

        for (day in 1..totalDays) {
            days.add(day.toString())
        }

        grid.adapter = CalendarAdapter(this, days, eventsMap, currentMonth)
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

    private fun getDateKey(day: Int): String {
        return "${currentMonth.year}-${currentMonth.monthValue}-%02d".format(day)
    }

    private fun saveEvent(day: Int, event: String) {
        val key = getDateKey(day)

        if (!eventsMap.containsKey(key)) {
            eventsMap[key] = mutableListOf()
        }

        eventsMap[key]?.add(event)

        updateUpcomingEvents()
    }

    private fun updateUpcomingEvents() {
        val event1 = findViewById<TextView>(R.id.event1)
        val event2 = findViewById<TextView>(R.id.event2)
        val event3 = findViewById<TextView>(R.id.event3)

        val edit1 = findViewById<ImageButton>(R.id.editEvent1)
        val rsvp1 = findViewById<ImageButton>(R.id.rsvpEvent1)
        val send1 = findViewById<ImageButton>(R.id.sendEvent1)

        val edit2 = findViewById<ImageButton>(R.id.editEvent2)
        val rsvp2 = findViewById<ImageButton>(R.id.rsvpEvent2)
        val send2 = findViewById<ImageButton>(R.id.sendEvent2)

        val edit3 = findViewById<ImageButton>(R.id.editEvent3)
        val rsvp3 = findViewById<ImageButton>(R.id.rsvpEvent3)
        val send3 = findViewById<ImageButton>(R.id.sendEvent3)

        val day = selectedDay

        if (day == null) {
            event1.text = "None"
            event2.text = ""
            event3.text = ""

            toggleEventButtons("", edit1, rsvp1, send1)
            toggleEventButtons("", edit2, rsvp2, send2)
            toggleEventButtons("", edit3, rsvp3, send3)
            return
        }

        val key = getDateKey(day)
        val events = eventsMap[key]

        if (events.isNullOrEmpty()) {
            event1.text = "None"
            event2.text = ""
            event3.text = ""

            toggleEventButtons("", edit1, rsvp1, send1)
            toggleEventButtons("", edit2, rsvp2, send2)
            toggleEventButtons("", edit3, rsvp3, send3)
            return
        }

        val e1 = events.getOrNull(0) ?: ""
        val e2 = events.getOrNull(1) ?: ""
        val e3 = events.getOrNull(2) ?: ""

        event1.text = e1
        event2.text = e2
        event3.text = e3

        toggleEventButtons(e1, edit1, rsvp1, send1)
        toggleEventButtons(e2, edit2, rsvp2, send2)
        toggleEventButtons(e3, edit3, rsvp3, send3)
    }

    private fun toggleEventButtons(
        eventText: String,
        edit: ImageButton,
        rsvp: ImageButton,
        send: ImageButton
    ) {
        val hasEvent = eventText.isNotBlank() && eventText != "None"

        val visibility = if (hasEvent) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        edit.visibility = visibility
        rsvp.visibility = visibility
        send.visibility = visibility
    }

    private fun setupEventButtons(
        editBtn: ImageButton,
        rsvpBtn: ImageButton,
        sendBtn: ImageButton,
        getEventText: () -> String
    ) {
        editBtn.setOnClickListener {
            val oldEvent = getEventText()
            if (oldEvent.isBlank()) {
                Toast.makeText(this, "No event here", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val input = android.widget.EditText(this)
            input.setText(oldEvent)

            android.app.AlertDialog.Builder(this)
                .setTitle("Edit Event")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newText = input.text.toString()

                    if (newText.isNotBlank()) {
                        updateEvent(oldEvent, newText)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rsvpBtn.setOnClickListener {
            val event = getEventText()
            if (event.isBlank() || event == "None") {
                Toast.makeText(this, "No event here", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "RSVP'd to: $event", Toast.LENGTH_SHORT).show()
        }

        sendBtn.setOnClickListener {
            val event = getEventText()
            if (event.isBlank() || event == "None") {
                Toast.makeText(this, "No event here", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //TODO Add logic to send event to a friend
            Toast.makeText(this, "Sent Friend request for: $event", Toast.LENGTH_SHORT).show()

            /*val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this event: $event")
            startActivity(Intent.createChooser(intent, "Send via"))
             */
        }
    }

    private fun updateEvent(oldValue: String, newValue: String) {
        val day = selectedDay ?: return
        val key = getDateKey(day)

        val list = eventsMap[key] ?: return
        val index = list.indexOf(oldValue)

        if (index != -1) {
            list[index] = newValue
            updateUpcomingEvents()
        }
    }

    private fun updateJoinUI(button: Button, isJoined: Boolean) {
        if (isJoined) {
            button.text = "Leave"
            button.setBackgroundColor(android.graphics.Color.GRAY)
        } else {
            button.text = "Join"
            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
        }
        //set text color to white regardless of join button status
        button.setTextColor(android.graphics.Color.WHITE)
    }

}
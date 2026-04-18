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

class TopicDetailActivity : AppCompatActivity() {
    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var grid: GridView
    private lateinit var monthTitle: TextView

    private val eventsMap = mutableMapOf<String, MutableList<String>>()

    private var selectedDay: Int? = null

    private var isJoined = false

    private var days = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        val addEventBtn = findViewById<ImageButton>(R.id.addEventBtn)

        val joinButton = findViewById<Button>(R.id.joinButton)
//        val name = intent.getStringExtra("topicName")
//        val desc = intent.getStringExtra("topicDesc")
//        val members = intent.getIntExtra("memberCount", 0)

        val topicID = intent.getStringExtra("topicID") ?: return
        //TODO: isJoined intent here, or in database query where everything else is set?
        //Use the isJoined status from the query in TopicsAdapter, default==false
        isJoined = intent.getBooleanExtra("isJoined", false)
        updateJoinUI(joinButton, isJoined)
//        val name = intent.getStringExtra("topicName") ?: return
//        val topicDesc = intent.getStringExtra("topicDesc") ?: return
//        val memberCount = intent.getStringExtra("memberCount") ?: return

        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    //If document exists for this topicID, load the needed fields from Firestore
                    val name = document.getString("topicName") ?: ""
                    val desc = document.getString("topicDesc") ?: ""
                    val memberCount = document.getLong("memberCount")?.toInt() ?: 0
                    val timestamp = document.getTimestamp("createdAt")
                    //load user membership

                    //TODO:Revisit if Joined (btn state) logic needs to come in here as well

                    // Convert Firestore timestamp into a readable string
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
                    //call join button ui update here
                }
            }
            .addOnFailureListener {
                println("TopicDetailsActivity listener failure for $topicID")
                Toast.makeText(this, "Failed to load topic", Toast.LENGTH_SHORT).show()
            }

        //TODO in FIRESTORE this is the creation date of when the topic was created
//        val createdAt = intent.getStringExtra("createdAt")
//        findViewById<TextView>(R.id.detailCreatedAt).text =
//            "$createdAt"
//
//        findViewById<TextView>(R.id.detailTopicName).text = name
//        findViewById<TextView>(R.id.detailDescription).text = desc
//        findViewById<TextView>(R.id.detailMembers).text = "$members members"

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

        //TODO: Fix join button logic to match list logic
        //join button logic, initial state set near top of onCreate
        joinButton.setOnClickListener {
                //on join button click
                if (!isJoined) {   //&& user is !joined on this Topic, call join Topic in Firestore
                    TopicRepository.joinTopic(topicID) { success ->
                        //If joinTopic==success, set isJoined==true and change button UI state
                        if (success) {
                            //need to update member count probably
                            isJoined = true
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
                            //need to update member count probably
                            isJoined = false
                            updateJoinUI(joinButton, isJoined)
                            Toast.makeText(this, "You left $topicID!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to leave", Toast.LENGTH_SHORT).show()
                            println("leave failed in Join btn press on success")
                        }
                    }
                }

//            isJoined = !isJoined

//            val membersText = findViewById<TextView>(R.id.detailMembers)
//            val currentText = membersText.text.toString().split(" ")[0].toInt()

//            if (isJoined) {
//                membersText.text = "${currentText + 1} members"
//                Toast.makeText(this, "You joined $topicID!", Toast.LENGTH_SHORT).show()
//            } else {
//                membersText.text = "${currentText - 1} members"
//                Toast.makeText(this, "You left $topicID!", Toast.LENGTH_SHORT).show()
//            }

//            updateJoinUI(joinButton, isJoined)
        }

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        //Enter Community Button in Topics Activity - Not the one in the NavBar
        val enterCommunityBtn = findViewById<Button>(R.id.communityBoardButton)

        enterCommunityBtn.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            startActivity(intent)
        }

        // Toolbar + Listeners
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
            startActivity(intent)
        }
        communityButton.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        guideButton.setOnClickListener {
            val intent = Intent(this, GuidesActivity::class.java)
            startActivity(intent)
        }

        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
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

        val day = selectedDay

        if (day == null) {
            event1.text = "None"
            event2.text = ""
            event3.text = ""
            return
        }

        val key = getDateKey(day)
        val events = eventsMap[key]

        if (events.isNullOrEmpty()) {
            event1.text = "None"
            event2.text = ""
            event3.text = ""
            return
        }

        event1.text = events.getOrNull(0) ?: ""
        event2.text = events.getOrNull(1) ?: ""
        event3.text = events.getOrNull(2) ?: ""
    }

    private fun updateJoinUI(button: Button, isJoined: Boolean) {
        if (isJoined) {
            button.text = "Leave"
//            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
            button.setBackgroundColor(android.graphics.Color.GRAY)
        } else {
            button.text = "Join"
//            button.setBackgroundColor(android.graphics.Color.GRAY)
            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
        }
        //set text color to white regardless of join button status
        button.setTextColor(android.graphics.Color.WHITE)
    }

}
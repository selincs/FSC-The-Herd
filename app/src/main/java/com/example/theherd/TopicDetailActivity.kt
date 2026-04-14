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

class TopicDetailActivity : AppCompatActivity() {
    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var grid: GridView
    private lateinit var monthTitle: TextView

    private var isJoined = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        val name = intent.getStringExtra("topicName")
        val joinButton = findViewById<Button>(R.id.joinButton)
        val desc = intent.getStringExtra("topicDesc")
        val members = intent.getIntExtra("memberCount", 0)

        //TODO in FIRESTORE this is the creation date of when the topic was created
        val createdAt = intent.getStringExtra("createdAt")
        findViewById<TextView>(R.id.detailCreatedAt).text =
            "$createdAt"

        findViewById<TextView>(R.id.detailTopicName).text = name
        findViewById<TextView>(R.id.detailDescription).text = desc
        findViewById<TextView>(R.id.detailMembers).text = "$members members"

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

        updateCalendar()

        //join button logic
        updateJoinUI(joinButton, isJoined)
        joinButton.setOnClickListener {
            isJoined = !isJoined

            val membersText = findViewById<TextView>(R.id.detailMembers)
            val currentText = membersText.text.toString().split(" ")[0].toInt()

            if (isJoined) {
                membersText.text = "${currentText + 1} members"
                Toast.makeText(this, "You joined $name!", Toast.LENGTH_SHORT).show()
            } else {
                membersText.text = "${currentText - 1} members"
                Toast.makeText(this, "You left $name!", Toast.LENGTH_SHORT).show()
            }

            updateJoinUI(joinButton, isJoined)
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

        val days = mutableListOf<String>()

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

        grid.adapter = CalendarAdapter(this, days)
    }

    private fun updateJoinUI(button: Button, isJoined: Boolean) {
        if (isJoined) {
            button.text = "Joined"
            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
        } else {
            button.text = "Join"
            button.setBackgroundColor(android.graphics.Color.GRAY)
        }
        button.setTextColor(android.graphics.Color.WHITE)
    }

}
package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog


class MotivationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation) // connects XML
        val homeButton: ImageButton = findViewById(R.id.homeButton)

        // toolbar buttons
        val eventsButton: Button = findViewById(R.id.events_button)
//        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)


        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        // button event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        motivationButton.setOnClickListener {
//            val intent = Intent(this, MotivationActivity::class.java)
//            startActivity(intent)
//        }
//
        friendsButton.setOnClickListener {
            val intent = Intent(this, FriendsListActivity::class.java)
            startActivity(intent)
        }
//
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


        // Settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        val mentorsRecyclerView = findViewById<RecyclerView>(R.id.mentorsRecyclerView)
        val commitmentsRecyclerView = findViewById<RecyclerView>(R.id.commitmentsRecyclerView)

        mentorsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        commitmentsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //Fake Data -- Can remove when firebase is connected
        val fakeMentors = listOf(Mentor("Rachel Green", "Mentor"), Mentor("Ross Geller", "Mentor"), Mentor("Monica Geller", "Mentor"), Mentor("Chandler Bing", "Mentor"), Mentor("Joey Tribbiani", "Mentor"), Mentor("Phoebe Buffay", "Mentor"))
        val fakeCommitments = listOf(Commitment("Go to Gym", "Chandler", 3), Commitment("Study", "Monica", 5), Commitment("Go for a hike", "Joey", 9), Commitment("Self Defense", "Rachel", 15))

        mentorsRecyclerView.adapter = MentorAdapter(fakeMentors)

        commitmentsRecyclerView.adapter = CommitmentAdapter(fakeCommitments) { clickedCommitment ->
            val bottomSheet = BottomSheetDialog(this)
            bottomSheet.setContentView(R.layout.bottom_sheet_commitment)

            val titleText = bottomSheet.findViewById<TextView>(R.id.detailActivityName)
            val partnerText = bottomSheet.findViewById<TextView>(R.id.detailPartnerName)
            val streakNum = bottomSheet.findViewById<TextView>(R.id.detailStreakNumber)

            titleText?.text = clickedCommitment.activityName
            partnerText?.text = "Shared commitment with ${clickedCommitment.partnerName}"
            streakNum?.text = "🔥 ${clickedCommitment.streak} Days"

            bottomSheet.show()
        }

    }
}

data class Mentor(val name: String, val role: String)
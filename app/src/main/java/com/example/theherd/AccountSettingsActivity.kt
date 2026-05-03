package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.app.AlertDialog
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AccountSettingsActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        recyclerView = findViewById(R.id.blockedUsersRecyclerView)
        emptyStateText = findViewById(R.id.tvNoBlockedUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        // event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)  //Change to EventsActivity or w.e it is named
//            startActivity(intent)
//        }
//
        motivationButton.setOnClickListener {
            val intent = Intent(this, MotivationActivity::class.java)
            startActivity(intent)
        }

        friendsButton.setOnClickListener {
            val intent = Intent(this, FriendsListActivity::class.java)
            startActivity(intent)
        }

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

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish()
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        settingsButton.setOnClickListener { view ->
            Toast.makeText(this, "Exit Account Settings if you wish to log out.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupBlockedList()
    }

    private fun setupBlockedList() {
        BlockListRepository.getBlockedUsers(
            onSuccess = { blockedList ->

                if (blockedList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE

                    recyclerView.adapter = BlockedFriendsAdapter(
                        blockedList.toMutableList()
                    ) { friend ->

                        AlertDialog.Builder(this)
                            .setTitle("Unblock ${friend.name}?")
                            .setMessage("They will be able to interact with you again.")
                            .setPositiveButton("Unblock") { _, _ ->

                                BlockListRepository.unblockUser(friend.id) { success ->
                                    if (success) {
                                        val adapter = recyclerView.adapter as BlockedFriendsAdapter

                                        if (adapter.itemCount == 0) {
                                            recyclerView.visibility = View.GONE
                                            emptyStateText.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            },
            onFailure = {
                emptyStateText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        )
    }
}
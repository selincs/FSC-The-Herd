package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AccountSettingsActivity : BaseActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        setupNavigation() // sets up all buttons in the tool/nav bar

        recyclerView = findViewById(R.id.blockedUsersRecyclerView)
        emptyStateText = findViewById(R.id.tvNoBlockedUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

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
    }

    override fun onResume() {
        super.onResume()
        setupBlockedList()
    }

    private fun setupBlockedList() {
        FriendsRepository.getBlockedUsers(
            onSuccess = { blockedList ->
                if (blockedList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE
                    recyclerView.adapter = BlockedFriendsAdapter(blockedList.toMutableList()) { friend ->
                        FriendsRepository.unblockUser(friend.id) { success ->
                            if (success) {
                                setupBlockedList()
                            }
                        }
                    }
                }
            }, onFailure = {
                Toast.makeText(this, "ERROR: Blocked user list can't be loaded", Toast.LENGTH_SHORT).show()
            }
        )
        /* val blockedList = MockFriendsRepo.getBlockedFriends().toMutableList()

        if (blockedList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = BlockedFriendsAdapter(blockedList) { friend ->
                MockFriendsRepo.unblockFriend(friend)
                if (MockFriendsRepo.getBlockedFriends().isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                }
            }
        } */
    }
}
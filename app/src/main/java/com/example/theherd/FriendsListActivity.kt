package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FriendsListActivity : AppCompatActivity() {

    private lateinit var adapter: FriendsAdapter
    private val repo = MockFriendsRepo

    private lateinit var searchInput: TextInputEditText
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        searchInput = findViewById(R.id.searchInput)
        val recyclerView = findViewById<RecyclerView>(R.id.friendsRecyclerView)
        val tabLayout = findViewById<TabLayout>(R.id.friendsTabLayout)
        val btnAddFriend = findViewById<MaterialButton>(R.id.btnAddFriend)

        // buttons
//        val eventsButton: Button = findViewById(R.id.events_button)
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

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        // button event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

        motivationButton.setOnClickListener {
            val intent = Intent(this, MotivationActivity::class.java)
            startActivity(intent)
        }
//
//        friendsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
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

        filterFriends("")

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterFriends(searchInput.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFriends(query: String) {
        val baseList = when (currentTab) {
            1 -> repo.getMockFriends().filter { it.isOnline }
            2 -> repo.getMockRequests()
            else -> repo.getMockFriends()
        }

        val filteredList = if (query.isEmpty()) {
            baseList
        } else {
            baseList.filter { it.name.contains(query, ignoreCase = true) }
        }

        val finalSortedList = if (currentTab != 2) {
            filteredList.sortedByDescending { it.isOnline }
        } else {
            filteredList
        }

        updateRecycler(finalSortedList)
    }

    private fun updateRecycler(newList: List<Friend>) {
        adapter = FriendsAdapter(newList.toMutableList()) { friendToRemove ->

            // 1. remove friend from repo
            repo.removeFriend(friendToRemove)

            // 2. refresh to ensure that the removed friend is no longer displayed
            filterFriends(searchInput.text.toString())
        }

        findViewById<RecyclerView>(R.id.friendsRecyclerView).adapter = adapter
    }

    private fun showAddFriendDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_friend, null)

        val btnSend = view.findViewById<MaterialButton>(R.id.btnSendRequest)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelAddFriend)
        val etInput = view.findViewById<TextInputEditText>(R.id.etAddFriendInput)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.addFriendInputLayout)

        btnSend.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isNotEmpty()) {
                // 1. Save the added friend to the repo
                repo.addFriendRequest(input)
                // 2. Tell the user the request was sent
                Toast.makeText(this, "Friend request sent to $input!", Toast.LENGTH_SHORT).show()
                // 3. refresh the view
                filterFriends(searchInput.text.toString())

                dialog.dismiss()
            } else {
                inputLayout.error = "Email or username required"
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        filterFriends(searchInput.text.toString())
    }
}
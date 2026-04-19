package com.example.theherd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
package com.example.theherd

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FriendsListActivity : BaseActivity() {

    private lateinit var adapter: FriendsAdapter
    private val repo = FriendsRepository

    private lateinit var searchInput: TextInputEditText
    private lateinit var filterSpinner: Spinner
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)

        searchInput = findViewById(R.id.searchInput)
        filterSpinner = findViewById(R.id.searchFilterSpinner)

        val recyclerView = findViewById<RecyclerView>(R.id.friendsRecyclerView)
        val tabLayout = findViewById<TabLayout>(R.id.friendsTabLayout)
        val btnAddFriend = findViewById<MaterialButton>(R.id.btnAddFriend)
        setupNavigation()

        ArrayAdapter.createFromResource(
            this,
            R.array.search_filters,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadTabData("")

        btnAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                loadTabData(searchInput.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        searchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val query = searchInput.text.toString().trim()
                val selectedFilter = filterSpinner.selectedItem.toString()

                if (query.isNotEmpty()) {
                    performGlobalSearch(query, selectedFilter)
                } else {
                    loadTabData("")
                }
                true
            } else {
                false
            }
        }
    }

    private fun loadTabData(query: String) {
        if (currentTab == 2) {
            repo.getAllRequests { requestList ->
                val filtered = if (query.isEmpty()) requestList
                else requestList.filter { it.name.contains(query, ignoreCase = true) }
                updateRecycler(filtered.sortedByDescending { it.isOnline })
            }
        } else {
            repo.loadFriends(
                onSuccess = { friends ->
                    val baseList = if (currentTab == 1) friends.filter { it.isOnline } else friends
                    val filtered = if (query.isEmpty()) baseList
                    else baseList.filter { it.name.contains(query, ignoreCase = true) }
                    updateRecycler(filtered.sortedByDescending { it.isOnline })
                },
                onFailure = {
                    Toast.makeText(this, "Failed to load friends", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun performGlobalSearch(query: String, filter: String) {
        repo.searchGlobalUsers(query, filter) { results ->
            if (results.isEmpty()) {
                Toast.makeText(this, "No users found in the Herd", Toast.LENGTH_SHORT).show()
            }
            updateRecycler(results)
        }
    }

    private fun updateRecycler(newList: List<Friend>) {
        adapter = FriendsAdapter(newList.toMutableList()) { _ ->
            loadTabData(searchInput.text.toString())
        }
        findViewById<RecyclerView>(R.id.friendsRecyclerView).adapter = adapter
    }

    private fun showAddFriendDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_friend, null)
        val btnSend = view.findViewById<MaterialButton>(R.id.btnSendRequest)
        val etInput = view.findViewById<TextInputEditText>(R.id.etAddFriendInput)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.addFriendInputLayout)

        btnSend.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isNotEmpty()) {
                repo.sendFriendRequest(input,
                    onSuccess = {
                        Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                        loadTabData("")
                        dialog.dismiss()
                    },
                    onFailure = { e -> inputLayout.error = e.message }
                )
            } else {
                inputLayout.error = "Input required"
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadTabData(searchInput.text.toString())
    }
}
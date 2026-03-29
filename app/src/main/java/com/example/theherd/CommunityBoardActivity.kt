package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class CommunityBoardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommunityAdapter
    private lateinit var searchView: SearchView

    private val allCommunities = ArrayList<Community>()
    private val displayList = ArrayList<Community>()
    private var isFilterActive = false

    private val startCreateCommunity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val name = result.data?.getStringExtra("COMMUNITY_NAME") ?: ""
            val desc = result.data?.getStringExtra("COMMUNITY_DESC") ?: ""

            if (name.isNotEmpty()) {
                val newCommunity = Community(name, desc, isJoined = true)
                allCommunities.add(0, newCommunity)

                PreferencesManager.saveAllCommunities(this, allCommunities)

                filterList(searchView.query.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_board)

        // buttons
        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

        // event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//
//        motivationButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
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
            println("In MainActivity: communityButton onclick listener")
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

        setupMenuButtons()

        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupNavigationButtons(toolbar)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener {
            finish() // Closes this page and goes back
        }

        recyclerView = findViewById(R.id.community_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = CommunityAdapter(displayList) { community ->
            val intent = Intent(this, SpecificCommunityActivity::class.java)
            intent.putExtra("COMMUNITY_NAME", community.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        setupSampleData()

        searchView = findViewById(R.id.communitySearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        val myListsBtn = findViewById<Button>(R.id.myListsButton)
        myListsBtn.setOnClickListener {
            isFilterActive = !isFilterActive
            myListsBtn.text = if (isFilterActive) "Show All" else "My Communities"
            filterList(searchView.query.toString())
        }

        findViewById<ExtendedFloatingActionButton>(R.id.fabAddCommunity).setOnClickListener {
            val intent = Intent(this, CreateCommunityActivity::class.java)
            startCreateCommunity.launch(intent)
        }

        val sortSpinner: Spinner = findViewById(R.id.sortDropdown)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortCommunities(true)  // Most Recent
                    1 -> sortCommunities(false) // Oldest
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMenuButtons() {
        findViewById<Button>(R.id.community_button).setOnClickListener {
        }
        findViewById<Button>(R.id.profile_button).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.guide_button).setOnClickListener {
            startActivity(Intent(this, GuidesActivity::class.java))
        }
    }

    private fun setupSampleData() {
        if (allCommunities.isEmpty()) {
            val savedList = PreferencesManager.loadAllCommunities(this)
            if (savedList.isNotEmpty()) {
                allCommunities.addAll(savedList)
            }
            filterList("")
        }
    }

    private fun filterList(query: String?) {
        displayList.clear()

        val sourceList = if (isFilterActive) {
            allCommunities.filter { it.isJoined }
        } else {
            allCommunities
        }

        if (query.isNullOrEmpty()) {
            displayList.addAll(sourceList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            sourceList.forEach {
                if (it.name.lowercase().contains(lowerCaseQuery)) {
                    displayList.add(it)
                }
            }
        }

        adapter.notifyDataSetChanged()

        val emptyText: TextView = findViewById(R.id.emptyStateText)

        if (displayList.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            when {
                !query.isNullOrEmpty() -> {
                    emptyText.text = "No results found for \"$query\""
                }
                isFilterActive -> {
                    emptyText.text = "It looks like you're flying solo! Join a community to see what's happening on campus."
                }
                else -> {
                    emptyText.text = "No communities yet. Be the first to start one!"
                }
            }
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupNavigationButtons(toolbar: Toolbar) {
        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun sortCommunities(mostRecent: Boolean) {
        if (mostRecent) {
            allCommunities.sortByDescending { it.createdAt }
        } else {
            allCommunities.sortBy { it.createdAt }
        }
        filterList(searchView.query.toString())
    }

    private fun updateEmptyState() {
        filterList(searchView.query.toString())
    }

    override fun onResume() {
        super.onResume()
        val updatedList = PreferencesManager.loadAllCommunities(this)
        allCommunities.clear()
        allCommunities.addAll(updatedList)
        filterList(searchView.query.toString())
    }
}
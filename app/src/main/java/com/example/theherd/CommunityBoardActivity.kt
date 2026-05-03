package com.example.theherd

import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
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
            //get  all the topics from topic collection
            loadCommunitiesFromFirestore()
        }
    }
    private fun loadCommunitiesFromFirestore(){
        //clears the old list.
        allCommunities.clear()
        val userId = FirestoreAuthManager.currentUserId
        if(userId == null){
            filterList(searchView.query.toString())
            return
        }
        FirebaseFirestore.getInstance()
            .collection("topics")
            .get()
            .addOnSuccessListener{
                result ->
                if (result.isEmpty){
                    filterList(searchView.query.toString())
                    return@addOnSuccessListener
                }
                var remainingChecks = result.size()

                for( doc in result){
                    val topicID = doc.id
                    val name = doc.getString("topicName") ?: ""
                    val desc = doc.getString("topicDesc") ?: ""
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: 0


                    FirebaseFirestore.getInstance()
                        .collection("topics")
                        .document(topicID)
                        .collection("members")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { memberDoc  ->
                            val community = Community(
                                topicID = topicID,
                                name = name,
                                description = desc,
                                memberCount = memberCount,
                                isJoined = memberDoc.exists()
                            )
                            allCommunities.add(community)

                            remainingChecks --

                            if( remainingChecks == 0){
                                filterList(searchView.query.toString())
                            }
                        }
                        .addOnFailureListener{
                            val community = Community(
                                topicID = topicID,
                                name = name,
                                description = desc,
                                memberCount = memberCount,
                                isJoined = false
                            )

                            allCommunities.add(community)

                            remainingChecks--

                            if(remainingChecks == 0 ){
                                filterList(searchView.query.toString())
                            }

                        }




                }
                filterList(searchView.query.toString())
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
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        setSupportActionBar(toolbar)

        // event listeners
//        eventsButton.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
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


        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
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


        setupMenuButtons()

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

        adapter = CommunityAdapter(
            displayList,  onCommunityClick  = { community ->
                val intent = Intent(this, SpecificCommunityActivity::class.java)
                intent.putExtra("COMMMUNITY_NAME", community.name)
                intent.putExtra("TOPIC_ID", community.topicID)
                startActivity(intent)
            },
            onJoinCLick =  { community ->
                toggleJoinState(community)
            }
        )
        recyclerView.adapter = adapter



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

    private fun toggleJoinState(community: Community) {
        if (!community.isJoined) {
            TopicRepository.joinTopic(community.topicID) { success ->
                runOnUiThread {
                    if (success) {
                        community.isJoined = true
                        community.memberCount += 1
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Joined ${community.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to join ${community.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            TopicRepository.leaveTopic(community.topicID) { success ->
                runOnUiThread {
                    if (success) {
                        community.isJoined = false
                        if (community.memberCount > 0) {
                            community.memberCount -= 1
                        }
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Left ${community.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to leave ${community.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
        loadCommunitiesFromFirestore()
    }
}
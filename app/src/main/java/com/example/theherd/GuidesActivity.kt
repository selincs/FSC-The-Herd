
package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        setSupportActionBar(toolbar)

        val backButton: ImageButton = findViewById(R.id.btnBack)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener { finish() }

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Bottom Navigation Buttons
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val createGuideButton: Button = findViewById(R.id.create_guide_button)

        motivationButton.setOnClickListener {
            startActivity(Intent(this, MotivationActivity::class.java))
        }

        friendsButton.setOnClickListener {
            startActivity(Intent(this, FriendsListActivity::class.java))
        }

        interestsButton.setOnClickListener {
            startActivity(Intent(this, TopicsActivity::class.java))
        }

        communityButton.setOnClickListener {
            startActivity(Intent(this, CommunityBoardActivity::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        createGuideButton.setOnClickListener {
            startActivity(Intent(this, CreateGuideActivity::class.java))
        }

        // Category Clicks → opens GuideMainPageActivity
        setupCategoryClick(R.id.navigation_guide, "Navigation")
        setupCategoryClick(R.id.travel_guide, "Travel")
        setupCategoryClick(R.id.academic_guide, "Academic")
        setupCategoryClick(R.id.financial_aid_guid, "Financial Aid")
        setupCategoryClick(R.id.housing_guides, "Housing")
        setupCategoryClick(R.id.clubs_guide, "Clubs")
        setupCategoryClick(R.id.health_wellness_guide, "Health & Wellness")
        setupCategoryClick(R.id.miscellaneous_guides, "Miscellaneous")
    }

    private fun setupCategoryClick(viewId: Int, categoryName: String) {
        findViewById<TextView>(viewId).setOnClickListener {
            val intent = Intent(this, GuideMainPageActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            startActivity(intent)
        }
    }
}
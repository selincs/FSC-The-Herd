package com.example.theherd

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {

    private fun navigateTo(cls: Class<*>) {
        if (this::class.java == cls) return

        val intent = Intent(this, cls)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
    }

    protected fun setupNavigation() {
        val toolbar: Toolbar? = findViewById(R.id.topToolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        findViewById<ImageButton>(R.id.homeButton)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.settingsButton)?.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        findViewById<ImageButton>(R.id.btnBack)?.apply {
            visibility = View.VISIBLE
            setOnClickListener { finish() }
        }

        setupNavButton(R.id.friends_button, FriendsListActivity::class.java)
        setupNavButton(R.id.interests_button, TopicsActivity::class.java)
        setupNavButton(R.id.community_button, CommunityBoardActivity::class.java)
        setupNavButton(R.id.profile_button, ProfileActivity::class.java)
        setupNavButton(R.id.guide_button, GuidesActivity::class.java)
        setupNavButton(R.id.motivation_button, MotivationActivity::class.java)
        setupNavButton(R.id.events_button, EventsActivity::class.java)
    }

    private fun setupNavButton(id: Int, cls: Class<*>) {
        findViewById<Button>(id)?.setOnClickListener { navigateTo(cls) }
    }
}
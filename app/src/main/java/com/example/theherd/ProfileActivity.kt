package com.example.theherd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import android.widget.PopupMenu

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // connects XML

        // Settings btn
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->

            // Creates popup menu connected to settings btn
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.settings_menu, popupMenu.menu)

            // Handles menu clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_logout -> {

                        //When settings btn clicked add a way to logout the user

                        // Goes to LoginActivity and clears back stack
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }
}
package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, 3000) // logo stay on for 3 seconds

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val eventsButton: Button = findViewById(R.id.events_button)

        eventsButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val motivationButton: Button = findViewById(R.id.motivation_button)

        motivationButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val friendsButton: Button = findViewById(R.id.friends_button)

        friendsButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val interestsButton: Button = findViewById(R.id.interests_button)

        interestsButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val communityButton: Button = findViewById(R.id.community_button)

        communityButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val profileButton: Button = findViewById(R.id.profile_button)

        profileButton.setOnClickListener {
            // Create the Intent (From this page, To SecondActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
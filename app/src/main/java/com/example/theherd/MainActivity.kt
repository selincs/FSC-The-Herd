package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : BaseActivity() {

    private var keepSplash = true

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StatusAdapterMain

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        //TODO:Might need to move splash delay to after the Log In check? Verify.
        //Start of Selin code - Singleton Session Manager existence check.
        // Check if user is logged in after successful Log In activity - Not sign up
        if (!SessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Prevent returning to MainActivity
            return
        }
        //End Selin entry

        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
        }, 3000) // logo stay on for 3 seconds

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupNavigation()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Set Welcome text to welcome user by first name
        val welcomeText = findViewById<TextView>(R.id.WelcomeText)
        SessionManager.getProfile()?.let { profile ->
            welcomeText.text = "Welcome ${profile.firstName}!"
        }

        // settings button code lives in SettingsMenuHelper->TopBarHelper for all listeners eventually?
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        val createPostButton: Button = findViewById(R.id.create_post_button)

        createPostButton.setOnClickListener {
            val intent = Intent(this, CreateStatusActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun loadPosts() {
        val uid = SessionManager.requireUserId()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("statusPosts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val posts = snapshot.documents.map { doc ->
                    Status(
                        doc.getString("content") ?: "",
                        doc.getLong("timestamp") ?: 0L
                    )
                }

                recyclerView = findViewById(R.id.post_container)
                adapter = StatusAdapterMain(posts)

                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
            }
    }
}
package com.example.theherd
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import android.view.View

class CreatePostActivityMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post_main)

        val postInput = findViewById<EditText>(R.id.post_input)
        val submitButton = findViewById<Button>(R.id.submit_post_button)

        //toolbar & nav
        val toolbar: Toolbar = findViewById(R.id.topToolbar)
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        val settingsButton: ImageButton = findViewById(R.id.settingsButton)

        val backButton = toolbar.findViewById<ImageButton>(R.id.btnBack)

        backButton.visibility = View.VISIBLE

        backButton.setOnClickListener {
            finish()
        }

        setSupportActionBar(toolbar)

        settingsButton.setOnClickListener { view ->
            SettingsMenuHelper.showSettingsMenu(this, view)
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val text = postInput.text.toString()

            if (text.isNotBlank()) {
                PostRepositoryMain.addPost(text)
                finish() // return to MainActivity
            }
        }
    }
}
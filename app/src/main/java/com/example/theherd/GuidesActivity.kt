package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GuidesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides) // connects XML

        // button
        val createGuideButton: Button = findViewById(R.id.create_guide_button)

        // onclick event listener
        createGuideButton.setOnClickListener {
            println("in createGuideButton onclick listener:")
            val intent = Intent(this, CreateGuideActivity::class.java)
            startActivity(intent)
        }
    }
}
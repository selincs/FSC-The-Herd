package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import android.view.View


class GuidesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides) // connects XML
        setupNavigation()

        val createGuideButton: Button = findViewById(R.id.create_guide_button)

        createGuideButton.setOnClickListener {
            println("in createGuideButton onclick listener:")
            val intent = Intent(this, CreateGuideActivity::class.java)
            startActivity(intent)
        }

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
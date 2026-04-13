package com.example.theherd

import Model.Guide
import Model.GuideRepository
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.theherd.databinding.ActivityGuideMainBinding
import com.example.theherd.GuidesAdapter


class GuideMainPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val eventsButton: Button = findViewById(R.id.events_button)
        val motivationButton: Button = findViewById(R.id.motivation_button)
        val friendsButton: Button = findViewById(R.id.friends_button)
        val interestsButton: Button = findViewById(R.id.interests_button)
        val communityButton: Button = findViewById(R.id.community_button)
        val profileButton: Button = findViewById(R.id.profile_button)
        val guideButton: Button = findViewById(R.id.guide_button)

        interestsButton.setOnClickListener {
            val intent = Intent(this, TopicsActivity::class.java)
            startActivity(intent)
        }

        communityButton.setOnClickListener {
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


        val incomingCategory = intent.getStringExtra("CATEGORY_NAME")

        if(incomingCategory != null) {
            when (incomingCategory) {
                "Navigation", "Travel" -> {
                    println("User is looking for: $incomingCategory -> Routing to Campus Services")
                }
                "Academic", "Financial Aid" -> {
                    println("User is looking for: $incomingCategory -> Routing to Academic Services")
                }
                "Housing", "Clubs", "Health & Wellness" -> {
                    println("User is looking for: $incomingCategory -> Routing to Housing Services")
                }
                "Miscellaneous" -> {
                    println("User is looking for: $incomingCategory -> Routing to Miscellaneous Services")
                }

            }

        }
    }


private fun setupRecyclerView() {
    val allGuides = Model.GuideRepository.getAllGuides()
    val incomingCategory = intent.getStringExtra("CATEGORY_NAME")

    val displayedGuides = if (incomingCategory != null) {
        allGuides.filter { it.category == incomingCategory }
    } else {
        allGuides
    }


    val campusGuides = displayedGuides.filter { it.category == "Navigation" || it.category == "Travel" }
    val academicGuides = displayedGuides.filter { it.category == "Academic" || it.category == "Financial Aid" }
    val studentLifeGuides = displayedGuides.filter { it.category == "Housing" || it.category == "Clubs" || it.category == "Health & Wellness" }
    val otherGuides = displayedGuides.filter { it.category == "Miscellaneous" || it.category == "Other (specify below)" }

    val campusAdapter = GuidesAdapter()
    val academicAdapter = GuidesAdapter()
    val studentLifeAdapter = GuidesAdapter()
    val otherAdapter = GuidesAdapter()

    binding.rvCampusServices.adapter = campusAdapter
    binding.rvAcademicServices.adapter = academicAdapter
    binding.rvStudentLife.adapter = studentLifeAdapter
    binding.rvOther.adapter = otherAdapter

    binding.rvCampusServices.layoutManager = GridLayoutManager(this, 2)
    binding.rvAcademicServices.layoutManager = GridLayoutManager(this, 2)
    binding.rvStudentLife.layoutManager = GridLayoutManager(this, 2)
    binding.rvOther.layoutManager = GridLayoutManager(this, 2)

    campusAdapter.submitList(campusGuides)
    academicAdapter.submitList(academicGuides)
    studentLifeAdapter.submitList(studentLifeGuides)
    otherAdapter.submitList(otherGuides)

    if (campusGuides.isEmpty()) {
        binding.tvCampusHeader.visibility = android.view.View.GONE
        binding.rvCampusServices.visibility = android.view.View.GONE
    } else {
        binding.tvCampusHeader.visibility = android.view.View.VISIBLE
        binding.rvCampusServices.visibility = android.view.View.VISIBLE
    }

    if (academicGuides.isEmpty()) {
        binding.tvAcademicHeader.visibility = android.view.View.GONE
        binding.rvAcademicServices.visibility = android.view.View.GONE
    } else {
        binding.tvAcademicHeader.visibility = android.view.View.VISIBLE
        binding.rvAcademicServices.visibility = android.view.View.VISIBLE
    }

    if (studentLifeGuides.isEmpty()) {
        binding.tvStudentLifeHeader.visibility = android.view.View.GONE
        binding.rvStudentLife.visibility = android.view.View.GONE
    } else {
        binding.tvStudentLifeHeader.visibility = android.view.View.VISIBLE
        binding.rvStudentLife.visibility = android.view.View.VISIBLE
    }

    if (otherGuides.isEmpty()) {
        binding.tvOtherHeader.visibility = android.view.View.GONE
        binding.rvOther.visibility = android.view.View.GONE
    } else {
        binding.tvOtherHeader.visibility = android.view.View.VISIBLE
        binding.rvOther.visibility = android.view.View.VISIBLE
    }
}


    private fun showSuggestDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.suggest_guide_dialogue, null)
        builder.setView(dialogView).show()
    }


}
package com.example.theherd

import Model.Guide
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.theherd.databinding.ActivityGuideMainBinding

class GuideMainPageActivity : BaseActivity() {

    private lateinit var binding: ActivityGuideMainBinding
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Navigation"

        loadGuides()
    }

    private fun loadGuides() {
        GuideRepository.getGuidesFromFirestore { guides ->
            setupRecyclerView(guides)
        }
    }

    private fun setupRecyclerView(allGuides: List<Guide>) {
        val displayedGuides = allGuides.filter {
            it.category == categoryName
        }

        val campusGuides = displayedGuides.filter {
            it.category == "Navigation" || it.category == "Travel"
        }

        val academicGuides = displayedGuides.filter {
            it.category == "Academic" || it.category == "Financial Aid"
        }

        val studentLifeGuides = displayedGuides.filter {
            it.category == "Housing" ||
                    it.category == "Clubs" ||
                    it.category == "Health & Wellness"
        }

        val otherGuides = displayedGuides.filter {
            it.category == "Miscellaneous" ||
                    it.category == "Other (specify below)"
        }

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

        binding.tvCampusHeader.visibility =
            if (campusGuides.isEmpty()) View.GONE else View.VISIBLE
        binding.rvCampusServices.visibility =
            if (campusGuides.isEmpty()) View.GONE else View.VISIBLE

        binding.tvAcademicHeader.visibility =
            if (academicGuides.isEmpty()) View.GONE else View.VISIBLE
        binding.rvAcademicServices.visibility =
            if (academicGuides.isEmpty()) View.GONE else View.VISIBLE

        binding.tvStudentLifeHeader.visibility =
            if (studentLifeGuides.isEmpty()) View.GONE else View.VISIBLE
        binding.rvStudentLife.visibility =
            if (studentLifeGuides.isEmpty()) View.GONE else View.VISIBLE

        binding.tvOtherHeader.visibility =
            if (otherGuides.isEmpty()) View.GONE else View.VISIBLE
        binding.rvOther.visibility =
            if (otherGuides.isEmpty()) View.GONE else View.VISIBLE
    }
}
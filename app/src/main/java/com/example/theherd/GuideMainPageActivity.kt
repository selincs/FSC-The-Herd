package com.example.theherd

import android.content.Intent
import android.os.Bundle
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

//        binding.btnSuggestGuide.setOnClickListener {
//            showSuggestDialog()
//        }
    }

    private fun setupRecyclerView() {
        val guidesAdapter = GuidesAdapter { guide ->
            val intent = Intent(this, GuideTemplateActivity::class.java)
            intent.putExtra("GUIDE_ID", guide.id)
            startActivity(intent)
        }

        binding.rvCampusServices.apply {
            layoutManager = GridLayoutManager(this@GuideMainPageActivity, 2)
            adapter = guidesAdapter
        }
    }

    private fun showSuggestDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.suggest_guide_dialogue, null)
        builder.setView(dialogView).show()
    }
}
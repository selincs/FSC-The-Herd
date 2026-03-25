package com.example.theherd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.theherd.databinding.ActivityGuideTemplateBinding

class GuideTemplateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuideTemplateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
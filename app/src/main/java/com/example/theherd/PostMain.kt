package com.example.theherd

data class PostMain(
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
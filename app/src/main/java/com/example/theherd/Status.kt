package com.example.theherd

data class Status(
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
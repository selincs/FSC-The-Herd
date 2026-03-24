package com.example.theherd

import java.io.Serializable

data class Comment(
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
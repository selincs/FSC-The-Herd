package com.example.theherd

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Community(
    val topicID: String,
    val name: String,
    val description: String,
    val creatorName: String = "Anonymous Student",
    var memberCount: Int = 0,
    var isJoined: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

package com.example.theherd


//For displaying the FriendProfile in FriendProfileActivity
data class Friend(
    val id: String,
    val name: String,
    val statusText: String,
    val isOnline: Boolean,
    val isMentor: Boolean = false,
    val isFriend: Boolean,
    val major: String = "Computer Science",
    val gradDate: String = "Spring 2026",
    val username: String = "ram_user",
    val bio: String = "No bio yet.",
    val sharedTopics: List<String> = listOf(),
    val allTopics: List<String> = listOf()
)

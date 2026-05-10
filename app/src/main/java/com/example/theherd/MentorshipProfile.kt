package com.example.theherd

data class MentorshipProfile(

    val userId: String = "",
    val username: String = "",
// user can be both mentor and mentee
    val roles: List<String> = emptyList(),

    val mentorshipTopics: List<String> = emptyList(),

    val bio: String = "",

    val major: String = "",
    val year: String = "",

    val commuter: Boolean = false,
    val transferStudent: Boolean = false,

    val timestamp: Long = System.currentTimeMillis(),

    val active: Boolean = true
)
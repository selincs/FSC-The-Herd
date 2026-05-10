package com.example.theherd

data class MentorshipProfile(

    val userId: String = "",
    val username: String = "",

    //Roles : Mentor or Mentee, a user can be each.
    val roles: List<String> = emptyList(),

    // Mentor-specific
    val mentorTopics: List<String> = emptyList(),
    val mentorBio: String = "",

    // Mentee-specific
    val menteeTopics: List<String> = emptyList(),
    val menteeBio: String = "",

    // Shared profile info, not varied by mentorship role
    val major: String = "",
    val year: String = "",
    val commuter: Boolean = false,
    val transferStudent: Boolean = false,
    val active: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
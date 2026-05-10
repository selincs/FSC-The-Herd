package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object MotivationRepository {
    private val db = FirebaseFirestore.getInstance()

    fun createOrUpdateMentorshipProfile(
        profile: MentorshipProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val currentUserId = SessionManager.requireUserId()

        // Force correct UID
        val updatedProfile = profile.copy(
            userId = currentUserId
        )

        db.collection("mentorship_profiles")
            .document(currentUserId)
            .set(updatedProfile)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getMentorshipProfile(
        onSuccess: (MentorshipProfile?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val currentUserId = SessionManager.requireUserId()

        db.collection("mentorship_profiles")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->

                val profile = document.toObject(MentorshipProfile::class.java)

                onSuccess(profile)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

}
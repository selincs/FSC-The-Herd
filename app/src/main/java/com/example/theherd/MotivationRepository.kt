package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore
import Model.Commitment

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

    fun createCommitment(
        commitment: Commitment,
        onSuccess: (Commitment) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val currentUserId = SessionManager.requireUserId()

        val docRef =
            db.collection("commitments").document()

        val updatedCommitment = commitment.copy(
            commitmentId = docRef.id,
            userId = currentUserId
        )

        docRef
            .set(updatedCommitment)
            .addOnSuccessListener {
                onSuccess(updatedCommitment)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getUserCommitments(
        onSuccess: (List<Commitment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val currentUserId = SessionManager.requireUserId()

        db.collection("commitments")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->

                val commitments =
                    documents.toObjects(Commitment::class.java)

                onSuccess(commitments)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun updateCommitment(
        commitment: Commitment,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection("commitments")
            .document(commitment.commitmentId)
            .set(commitment)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

}
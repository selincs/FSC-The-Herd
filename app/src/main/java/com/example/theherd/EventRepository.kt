package com.example.theherd

import com.google.firebase.firestore.FirebaseFirestore

object EventRepository {
    fun createEvent(
        topicId: String,
        dateKey: String,
        event: Event,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "name" to event.name,
            "location" to event.location,
            "time" to event.time,
            "date" to dateKey,
            "hostId" to event.hostId,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("topics")
            .document(topicId)
            .collection("events")
            .add(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
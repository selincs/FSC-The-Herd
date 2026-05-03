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

    fun getEventsForTopic(
        topicId: String,
        onSuccess: (List<Pair<String, Event>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicId)
            .collection("events")
            .get()
            .addOnSuccessListener { result ->

                val events = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val location = doc.getString("location") ?: ""
                    val time = doc.getString("time") ?: ""
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val hostId = doc.getString("hostId") ?: ""

                    val event = Event(name, location, time, hostId)

                    date to event // Pair(dateKey, Event)
                }

                onSuccess(events)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
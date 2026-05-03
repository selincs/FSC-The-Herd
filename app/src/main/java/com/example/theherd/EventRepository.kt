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

        val docRef = db.collection("topics")
            .document(topicId)
            .collection("events")
            .document() // generate document ID first for the new Event

        // assign the eventID to the local Event object
        event.id = docRef.id

        val data = hashMapOf(
            "name" to event.name,
            "location" to event.location,
            "time" to event.time,
            "date" to dateKey,
            "hostId" to event.hostId,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        //Save data to new Event document
        docRef.set(data)
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

                    val event = Event(
                        id = doc.id,
                        name = name,
                        location = location,
                        time = time,
                        hostId = hostId
                    )

                    date to event
                }

                onSuccess(events)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateEventName(
        topicId: String,
        eventId: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicId)
            .collection("events")
            .document(eventId)
            .update("name", newName)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
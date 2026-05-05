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
        event.topicId = topicId

        val data = hashMapOf(
            "name" to event.name,
            "location" to event.location,
            "time" to event.time,
            "date" to dateKey,
            "hostId" to event.hostId,
            "rsvpCount" to event.rsvpCount,
            "rsvpUserIds" to event.rsvpUserIds,
            "topicId" to event.topicId,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        println("Created event with Topic ID : ${event.topicId}")

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
                    val rsvpCount = doc.getLong("rsvpCount")?.toInt() ?: 0
                    val topicIdFromDoc = doc.getString("topicId") ?: ""

                    val rsvpUserIds = doc.get("rsvpUserIds") as? List<String> ?: emptyList()

                    val event = Event(
                        id = doc.id,
                        name = name,
                        location = location,
                        time = time,
                        hostId = hostId,
                        date = date,
                        rsvpCount = rsvpCount,
                        rsvpUserIds = rsvpUserIds.toMutableList(),
                        topicId = topicIdFromDoc
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

    fun updateRsvp(
        topicId: String,
        eventId: String,
        userIds: List<String>,
        count: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("topics")
            .document(topicId)
            .collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "rsvpUserIds" to userIds,
                    "rsvpCount" to count
                )
            )
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getSingleEvent(
        topicId: String,
        eventId: String,
        onSuccess: (Event) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("topics")
            .document(topicId)
            .collection("events")
            .document(eventId)
            .get()
            .addOnSuccessListener { doc ->

                val event = Event(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    location = doc.getString("location") ?: "",
                    time = doc.getString("time") ?: "",
                    hostId = doc.getString("hostId") ?: "",
                    date = doc.getString("date") ?: "",
                    rsvpCount = doc.getLong("rsvpCount")?.toInt() ?: 0,
                    rsvpUserIds = (doc.get("rsvpUserIds") as? List<String>)?.toMutableList() ?: mutableListOf(),
                    topicId = doc.getString("topicId") ?: ""
                )

                onSuccess(event)
            }
            .addOnFailureListener { onFailure(it) }
    }
}
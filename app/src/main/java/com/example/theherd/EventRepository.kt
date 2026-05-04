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

    //AVNOOR EVENTS CODE BELOW:
    // key = "topicId_YYYY-MM-DD"
    private val events = mutableMapOf<String, MutableList<String>>()

    fun addEvent(topicId: String, dateKey: String, event: String) {
        val fullKey = "${topicId}_$dateKey"

        if (!events.containsKey(fullKey)) {
            events[fullKey] = mutableListOf()
        }
        events[fullKey]?.add(event)
    }

    fun getEventsForTopic(topicId: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        events.forEach { (key, eventList) ->
            if (key.startsWith(topicId)) {
                val date = key.removePrefix("${topicId}_")
                eventList.forEach { event ->
                    result.add(date to event)
                }
            }
        }

        return result
    }

    fun updateEvent(topicId: String, date: String, oldEvent: String, newEvent: String) {
        val key = "${topicId}_$date"
        val list = events[key] ?: return

        val index = list.indexOf(oldEvent)
        if (index != -1) {
            list[index] = newEvent
        }
    }

    fun getEventsForDay(topicId: String, dateKey: String): List<String> {
        return events["${topicId}_$dateKey"] ?: emptyList()
    }
}
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

    //Used only in message event invites, but could be refactored to handle TopicDetailActivity/EventActivity rsvp's as well
    fun toggleRsvp(
        event: Event,
        onComplete: (Boolean, Boolean) -> Unit
    ) {
        println("toggleRSVP entered")
        val db = FirebaseFirestore.getInstance()

        val userId = SessionManager.requireUserId()

        // ----------------------------------------
        // FETCH REAL EVENT FROM FIRESTORE
        // ----------------------------------------
        db.collection("topics")
            .document(event.topicId)
            .collection("events")
            .document(event.id)
            .get()

            .addOnSuccessListener { doc ->
                println("Firestore doc exists = ${doc.exists()}")
                println("Firestore raw data = ${doc.data}")

                val firestoreEvent =doc.toObject(Event::class.java)
                firestoreEvent?.id = doc.id //store the firestore event's document ID as well
                //it is not automatically stored and this must be done
                println("firestoreEvent = $firestoreEvent")

                if (firestoreEvent == null) {

                    onComplete(false, false)
                    return@addOnSuccessListener
                }

                val alreadyRsvpd = firestoreEvent.rsvpUserIds.contains(userId)
                // ----------------------------------------
                // UN-RSVP
                // ----------------------------------------
                if (alreadyRsvpd) {

                    firestoreEvent.rsvpUserIds.remove(userId)
                    firestoreEvent.rsvpCount -= 1

                } else {

                    // ----------------------------------------
                    // RSVP
                    // ----------------------------------------
                    firestoreEvent.rsvpUserIds.add(userId)
                    firestoreEvent.rsvpCount += 1
                }

                // ----------------------------------------
                // UPDATE FIRESTORE
                // ----------------------------------------
                updateRsvp(
                    firestoreEvent.topicId,
                    firestoreEvent.id,
                    firestoreEvent.rsvpUserIds,
                    firestoreEvent.rsvpCount
                ) { success ->

                    if (success) {

                        // ----------------------------------------
                        // USER SAVED EVENTS
                        // ----------------------------------------
                        if (alreadyRsvpd) {

                            UserRepository.removeUserEvent(
                                userId,
                                firestoreEvent.id
                            )

                            println("Removing RSVP")

                        } else {

                            UserRepository.addUserEvent(
                                userId,
                                firestoreEvent
                            )
                            println("Adding RSVP")
                        }

                        onComplete(true, !alreadyRsvpd)

                    } else {
                        onComplete(false, alreadyRsvpd)
                    }
                }
            }

            .addOnFailureListener {
                println("Failed to fetch event: ${it.message}")
                onComplete(false, false)
            }
        println("toggleRSVP exit")
    }

    //gets user rsvp status for button state of event message invites
    fun isUserRsvpd(
        topicId: String,
        eventId: String,
        onResult: (Boolean) -> Unit
    ) {

        val userId = SessionManager.requireUserId()

        FirebaseFirestore.getInstance()
            .collection("topics")
            .document(topicId)
            .collection("events")
            .document(eventId)
            .get()

            .addOnSuccessListener { doc ->

                val rsvpList =
                    doc.get("rsvpUserIds") as? List<String>
                        ?: emptyList()

                onResult(rsvpList.contains(userId))
            }

            .addOnFailureListener {
                onResult(false)
            }
    }
}
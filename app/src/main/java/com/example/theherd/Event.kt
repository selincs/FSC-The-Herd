package com.example.theherd

data class Event(
    var id: String = "",
    var name: String,
    var location: String = "",
    var time: String = "",
    var hostId: String = ""
    //month of event
    //day of event
    //year of event
    //rsvpCount
)
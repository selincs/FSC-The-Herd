package com.example.theherd

data class Event(
    var id: String = "",
    var name: String,
    var location: String = "",
    var time: String = "",
    var hostId: String = "",
    var date: String = "", //year/month/day of event == date of event as a string--dateKey == YYYY-M-D
    var rsvpCount: Int = 0,
    var rsvpUserIds: MutableList<String> = mutableListOf(),
    var topicId: String = "" //topicName, which is equivalent to topicID
)
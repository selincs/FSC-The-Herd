package com.example.theherd

data class Event(
    var name: String,
    var location: String = "",
    var time: String = "",
    var hostId: String = ""
)
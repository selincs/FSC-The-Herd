package com.example.theherd

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,    //server side time stamps to guarantee message history order


    val type: String = "text",  //differentiate between normal text messages, or sent event invites to a friend

    //optional class fields used only for event invite messages sent to a friend
    val eventId: String? = null,
    val eventName: String? = null,
    val eventTime: String? = null,
    val eventLocation: String? = null,
    val eventDate: String? = null,
    val topicId: String? = null
)


package com.example.theherd

import java.io.Serializable

data class Post(
    val title: String,
    val content: String,
    val author: String,
    var comments: ArrayList<Comment> = ArrayList()
) : Serializable
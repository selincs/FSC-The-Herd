package com.example.theherd

object PostRepositoryMain {
    val posts = mutableListOf<PostMain>()

    fun addPost(content: String) {
        posts.add(0, PostMain(content)) // newest on top
    }
}
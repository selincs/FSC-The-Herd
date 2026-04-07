package com.example.theherd

import com.google.firebase.auth.FirebaseAuth

object FirestoreAuthManager {
        //Initialize Firebase Auth for secure sign up and sign in
        val auth: FirebaseAuth by lazy {
            FirebaseAuth.getInstance()
        }

    //Helper that gets the currently logged-in user's UID - Can I replace Session Manager soon with this? Prob need both
    val currentUserId: String?
        get() = auth.currentUser?.uid

    //If CREATING A post - use val userId = FirestoreAuthManager.currentUserId ?: return to get currentUserId
    //Joining a topic, liking a post, anything a user does essentially

}
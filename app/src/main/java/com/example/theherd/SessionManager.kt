package com.example.theherd

import Model.User
import Model.Profile
import Model.Profile.OnlineStatus

object SessionManager {

    var currentUser: User? = null
        private set

    var currentProfile: Profile? = null
        private set

    //Quick access to Firebase UID without Firebase Reads
    val currentUserId: String?
        get() = FirestoreAuthManager.auth.currentUser?.uid

    //Login Fnc, sets enum to online
    fun login(user: User, profile: Profile) {
        currentUser = user
        currentProfile = profile
        currentProfile?.setOnlineStatus(OnlineStatus.ONLINE)   //Set user status to Online
        //Other on log in status change stuff can go here
        println("Session started for user: ${profile.userID}")
        println("Current userID= " + currentUserId)
    }

    //Logout clears session AND firebase auth
    //This function is called in MainActivity -> R.id.menu_logout
    fun logout() {
        //Set User status to Offline
        currentProfile?.setOnlineStatus(OnlineStatus.OFFLINE)

        FirestoreAuthManager.auth.signOut()

        currentUser = null
        currentProfile = null

        println("Session cleared in SessionMgr/Fstore, User logged out")
    }

    //Check login status via Firestore Auth Mgr
    //Check login status
    fun isLoggedIn(): Boolean {
        return FirestoreAuthManager.auth.currentUser != null
    }

    fun getProfile(): Profile? = currentProfile

    //Used when a function REQUIRES a logged-in user
    fun requireUserId(): String {
        return currentUserId ?: throw IllegalStateException("User not logged in")
    }
}
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
        //Set user status to Online
        currentProfile?.setOnlineStatus(OnlineStatus.ONLINE)
        //Other on log in status change stuff can go here
        println("Session started for user: ${profile.userID}")
    }

    //Logout clears session AND firebase auth
    fun logout() {
        //Set User status to Offline
        currentProfile?.setOnlineStatus(OnlineStatus.OFFLINE)

        FirestoreAuthManager.auth.signOut()

        currentUser = null
        currentProfile = null

        println("Session cleared in SessionMgr")
    }

    //Log the current user into a session, only via SessionManager
    fun isLoggedIn(): Boolean {
        //isLoggedIn doesnt check if currentProfile is null atm
        return currentUser != null
        //return currentUser != null && currentProfile != null
    }

    fun getUser(): User? = currentUser
    fun getProfile(): Profile? = currentProfile

    //Used when a function REQUIRES a logged-in user
    fun requireUserId(): String {
        return currentUserId ?: throw IllegalStateException("User not logged in")
    }
}
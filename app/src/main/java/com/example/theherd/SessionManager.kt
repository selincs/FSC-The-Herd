package com.example.theherd

import Model.User
import Model.Profile
import Model.Profile.OnlineStatus

object SessionManager {

    var currentUser: User? = null
        private set

    var currentProfile: Profile? = null
        private set

    //Login Fnc, sets enum to online
    fun login(user: User, profile: Profile) {
        currentUser = user
        currentProfile = profile
        //Set user status to Online
        currentProfile?.getOnlineStatus(OnlineStatus.ONLINE)
        //Other on log in status change stuff can go here

    }

    //Log the current user out of a session, only via SessionManager
    fun logout() {
        //Set user status to offline
        currentProfile?.getOnlineStatus(OnlineStatus.OFFLINE)
        //Other on log out status change stuff can go here

        currentUser = null
        currentProfile = null
    }

    //Log the current user into a session, only via SessionManager
    fun isLoggedIn(): Boolean {
        //isLoggedIn doesnt check if currentProfile is null atm
        return currentUser != null
        //return currentUser != null && currentProfile != null
    }

    fun getUser(): User? = currentUser
    fun getProfile(): Profile? = currentProfile
}
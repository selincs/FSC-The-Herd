package com.example.theherd

import Model.User
import Model.Profile

object FakeUserDatabase {
    private val users = mutableListOf<User>()
    private val profiles = mutableListOf<Profile>()

    init {
        // Create Test User
        println("FakeUserDatabase initialized, Test set")
        val testUser = User("test@farmingdale.edu", "Test")

        val testProfile = Profile(
            testUser.getUserID(),
            "Test",
            "Farm"
        )

        users.add(testUser)
        profiles.add(testProfile)
    }

    fun addUser(user: User, profile: Profile) {
        users.add(user)
        profiles.add(profile)
    }

    fun findUserByEmail(email: String): User? {
        return users.find { it.getFscEmail() == email }
    }

    fun getProfileByUserId(userId: String): Profile? {
        return profiles.find { it.getUserID() == userId }
    }

    fun validateLogin(email: String, password: String): Boolean {
        println("Validating login in FUDB")
        val user = findUserByEmail(email)
        return user != null && user.getPassword() == password
    }
}
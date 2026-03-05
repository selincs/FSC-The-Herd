package com.example.theherd

object UserRepository {
    private const val USE_FIRESTORE = true;
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onDone: (Boolean) -> Unit
    ) {
        // For now: use the fake database so everything works immediately
        val user = Model.User(email, password)
        val profile = Model.Profile(user.getUserID(), firstName, lastName)

        FakeUserDatabase.addUser(user, profile)
        onDone(true)
    }
}
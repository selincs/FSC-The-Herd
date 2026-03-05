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
        val user = Model.User(email, password)
        val userId = user.getUserID()
        val profile = Model.Profile(userId, firstName, lastName)

        if (!USE_FIRESTORE) {
            FakeUserDatabase.addUser(user, profile)
            onDone(true)
            return
        }

        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "password" to password
        )

        val profileData = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName
        )

        FirestoreDatabase.users.document(userId)
            .set(userData)
            .addOnSuccessListener {
                FirestoreDatabase.profiles.document(userId)
                    .set(profileData)
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { e ->
                        println("Firestore profiles write FAILED: ${e.message}")
                        onDone(false)
                    }
            }
            .addOnFailureListener { e ->
                println("Firestore users write FAILED: ${e.message}")
                onDone(false)
            }
    }
    fun login(email: String, password: String, onDone: (Boolean) -> Unit) {

        if (!USE_FIRESTORE) {
            // old fake login
            val ok = FakeUserDatabase.validateLogin(email, password)
            if (!ok) { onDone(false); return }

            val user = FakeUserDatabase.findUserByEmail(email) ?: run { onDone(false); return }
            val profile = FakeUserDatabase.getProfileByUserId(user.getUserID()) ?: run { onDone(false); return }

            SessionManager.login(user, profile)
            onDone(true)
            return
        }

        // Firestore login
        FirestoreDatabase.users
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onDone(false)
                    return@addOnSuccessListener
                }

                val doc = query.documents[0]

                val userId = doc.getString("userId") ?: run { onDone(false); return@addOnSuccessListener }
                val savedPassword = doc.getString("password") ?: run { onDone(false); return@addOnSuccessListener }
                val savedEmail = doc.getString("email") ?: email

                if (savedPassword != password) {
                    onDone(false)
                    return@addOnSuccessListener
                }

                // Load profile
                FirestoreDatabase.profiles.document(userId)
                    .get()
                    .addOnSuccessListener { profileDoc ->
                        if (!profileDoc.exists()) {
                            onDone(false)
                            return@addOnSuccessListener
                        }

                        val firstName = profileDoc.getString("firstName") ?: ""
                        val lastName = profileDoc.getString("lastName") ?: ""

                        // Create your Java model objects for SessionManager
                        val userObj = Model.User(savedEmail, savedPassword) // creates a NEW uuid...
                        // We need the real userId in memory, so for now we will NOT rely on User.userID.
                        // Instead, SessionManager mainly uses Profile.userID links, so we store correct userId there:
                        val profileObj = Model.Profile(userId, firstName, lastName)

                        SessionManager.login(userObj, profileObj)
                        onDone(true)
                    }
                    .addOnFailureListener { onDone(false) }
            }
            .addOnFailureListener { onDone(false) }
    }
}
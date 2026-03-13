package com.example.theherd

object UserRepository {
    private const val USE_FIRESTORE = true

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        onDone: (Boolean) -> Unit
    ) {
        val user = Model.User(email, "")
        val userId = user.getUserID()
        val profile = Model.Profile(userId, firstName, lastName)

        if (!USE_FIRESTORE) {
            FakeUserDatabase.addUser(user, profile)
            onDone(true)
            return
        }

        val userData = hashMapOf(
            "userId" to userId,
            "fscEmail" to email
        )

        val profileData = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName
        )

        FirestoreDatabase.users.document(userId)
            .set(userData)
            .addOnSuccessListener {
                FirestoreDatabase.users
                    .document(userId)
                    .collection("profile")
                    .document(userId)
                    .set(profileData)
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener { e ->
                        println("Firestore profile write FAILED: ${e.message}")
                        onDone(false)
                    }
            }
            .addOnFailureListener { e ->
                println("Firestore user write FAILED: ${e.message}")
                onDone(false)
            }
    }

    fun login(email: String, password: String, onDone: (Boolean) -> Unit) {

        if (!USE_FIRESTORE) {
            val ok = FakeUserDatabase.validateLogin(email, password)
            if (!ok) {
                onDone(false)
                return
            }

            val user = FakeUserDatabase.findUserByEmail(email) ?: run {
                onDone(false)
                return
            }

            val profile = FakeUserDatabase.getProfileByUserId(user.getUserID()) ?: run {
                onDone(false)
                return
            }

            SessionManager.login(user, profile)
            onDone(true)
            return
        }

        FirestoreDatabase.users
            .whereEqualTo("fscEmail", email)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onDone(false)
                    return@addOnSuccessListener
                }

                val doc = query.documents[0]

                val userId = doc.getString("userId") ?: run {
                    onDone(false)
                    return@addOnSuccessListener
                }



                val savedEmail = doc.getString("fscEmail") ?: email


                FirestoreDatabase.users
                    .document(userId)
                    .collection("profile")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { profileDoc ->
                        if (!profileDoc.exists()) {
                            onDone(false)
                            return@addOnSuccessListener
                        }

                        val firstName = profileDoc.getString("firstName") ?: ""
                        val lastName = profileDoc.getString("lastName") ?: ""

                        val userObj = Model.User(savedEmail, "")
                        val profileObj = Model.Profile(userId, firstName, lastName)

                        SessionManager.login(userObj, profileObj)
                        onDone(true)
                    }
                    .addOnFailureListener {
                        onDone(false)
                    }
            }
            .addOnFailureListener {
                onDone(false)
            }
    }
}
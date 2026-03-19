package com.example.theherd

import com.google.firebase.auth.FirebaseAuth

//this repository handles user registration and login logic.
object UserRepository {

    //toggle to switch between fake database and firestore.
    private const val USE_FIRESTORE = true

    //firebase authentication instance used to create and login users.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    //register a new user in firebase and optionally firestore.
    fun register(

        //function parameters.
        firstName: String,
        lastName: String,
        email: String,
        password: String,

        //callback function to report if the registration worked or failed.
        onDone: (Boolean) -> Unit
    ) {

        //create a new authentication account in firebase using email and password.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                //retrieve the user id that firebase generated.
                //if userid does not exist, stop and return failure.
                val userId = authResult.user?.uid ?: run {
                    onDone(false)
                    return@addOnSuccessListener
                }

                //create a profile object for the app's model.
                //this stores the user's personal info in our system.
                val profile = Model.Profile(userId, firstName, lastName)

                //if firestore storage is disabled we just stop here and return success.
                if (!USE_FIRESTORE) {
                    onDone(true)
                    return@addOnSuccessListener
                }

                //data for the main user document in firestore.
                //this collection stores basic account information.
                val userData = hashMapOf(
                    "userId" to userId,
                    "fscEmail" to email
                )

                //data for the user's profile subcollection document.
                //this stores additional personal info like names.
                val profileData = hashMapOf(
                    "userId" to userId,
                    "firstName" to firstName,
                    "lastName" to lastName
                )

                //write the main user document to firestore.
                FirestoreDatabase.users.document(userId)
                    .set(userData)
                    .addOnSuccessListener {

                        //after the user document is created,
                        //create the profile document inside a subcollection.
                        FirestoreDatabase.users
                            .document(userId)
                            .collection("profile")
                            .document(userId)
                            .set(profileData)

                            //profile write succeeded.
                            .addOnSuccessListener {
                                onDone(true)
                            }

                            //profile write failed.
                            .addOnFailureListener { e ->
                                println("Firestore profile write FAILED: ${e.message}")
                                onDone(false)
                            }

                    }

                    //main user document write failed.
                    .addOnFailureListener { e ->
                        println("Firestore user write FAILED: ${e.message}")
                        onDone(false)
                    }

            }

            //firebase authentication account creation failed.
            .addOnFailureListener { e ->
                println("Firebase Auth create user FAILED: ${e.message}")
                onDone(false)
            }
    }


    //login function authenticates user and loads profile.
    fun login(email: String, password: String, onDone: (Boolean) -> Unit) {

        //if firestore is disabled we use the fake local database.
        if (!USE_FIRESTORE) {

            //check if login credentials are correct.
            val ok = FakeUserDatabase.validateLogin(email, password)
            if (!ok) {
                onDone(false)
                return
            }

            //find the user object by email.
            val user = FakeUserDatabase.findUserByEmail(email) ?: run {
                onDone(false)
                return
            }

            //retrieve the profile using the user id.
            val profile = FakeUserDatabase.getProfileByUserId(user.getUserID()) ?: run {
                onDone(false)
                return
            }

            //store the logged in session.
            SessionManager.login(user, profile)

            onDone(true)
            return
        }

        //firebase authentication login using email and password.
        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener { authResult ->

                //get the firebase generated user id.
                val userId = authResult.user?.uid ?: run {
                    onDone(false)
                    return@addOnSuccessListener
                }

                //retrieve saved email from firebase auth.
                val savedEmail = authResult.user?.email ?: email

                //fetch the profile document from firestore.
                FirestoreDatabase.users
                    .document(userId)
                    .collection("profile")
                    .document(userId)
                    .get()

                    .addOnSuccessListener { profileDoc ->

                        //if the profile document does not exist login fails.
                        if (!profileDoc.exists()) {
                            println("Profile missing for user: $userId")
                            onDone(false)
                            return@addOnSuccessListener
                        }

                        //retrieve first and last name fields.
                        val firstName = profileDoc.getString("firstName") ?: ""
                        val lastName = profileDoc.getString("lastName") ?: ""

                        //create user and profile model objects.
                        val userObj = Model.User(savedEmail, "")
                        val profileObj = Model.Profile(userId, firstName, lastName)

                        //store session for the logged in user.
                        SessionManager.login(userObj, profileObj)

                        //login successful.
                        onDone(true)
                    }

                    //profile fetch failed.
                    .addOnFailureListener {
                        onDone(false)
                    }
            }

            //firebase login failed.
            .addOnFailureListener {
                onDone(false)
            }
    }
}
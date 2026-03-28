package com.example.theherd

import com.google.firebase.Timestamp


//this repository handles user registration and login logic.
object UserRepository {

    //toggle to switch between fake database and firestore.
    private const val USE_FIRESTORE = true

    //firebase authentication instance used to create and login users.
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    //register a new user in Firestore.
    ///User presses SignUp btn -> FAuth + createUserEmail+Pw -> UserRepo writes Firestore Document
    fun register(
        //function parameters.
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        graduationDate: String,
        onDone: (Boolean) -> Unit
    ) {

        //create a new authentication account in firebase using email and password.
        FirestoreAuthManager.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                //retrieve the user id that firebase generated.
                //if userid does not exist, stop and return failure.
                val userId = authResult.user?.uid ?: run {
                    onDone(false)
                    return@addOnSuccessListener
                }

                //data for the main user document in firestore.
                //this stores basic account information.
                val userData = hashMapOf(
                    "userID" to userId,
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "graduationDate" to graduationDate,
                    "major" to "",
                    "bio" to "",
                    "interests" to emptyList<String>(),
                    "profilePictureURL" to "",
                    "onlineStatus" to "online",
                    "createdAt" to Timestamp.now()
                )

                //write the main user document to firestore.
                FirestoreDatabase.users
                    .document(userId)
                    .set(userData)

                    .addOnSuccessListener {
                        println("User successfully created in Firestore")
                        onDone(true)
                    }

                    .addOnFailureListener { e ->
                        println("Firestore user creation failed: ${e.message}")
                        onDone(false)
                    }
            }

            .addOnFailureListener { e ->
                println("Auth registration failed: ${e.message}")
                onDone(false)
            }
    }


    //login function authenticates user and loads profile.
    //User presses login -> Fauth signIn -> Firebase returns UID -> Fstore loads user by uid -> SessMgr stores logged in User
    fun login(email: String, password: String, onDone: (Boolean) -> Unit) {
        //firebase authentication login using email and password.
        FirestoreAuthManager.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                //get the firebase generated user id.
                val userId = authResult.user?.uid ?: run {
                    onDone(false)
                    return@addOnSuccessListener
                }

                //retrieve saved email from firebase auth.
                val savedEmail = authResult.user?.email ?: email

                //fetch user document from firestore
                FirestoreDatabase.users
                    .document(userId)
                    .get()

                    .addOnSuccessListener { userDoc ->

                        if (!userDoc.exists()) {
                            println("User document missing for uid: $userId")
                            onDone(false)
                            return@addOnSuccessListener
                        }

                        val firstName = userDoc.getString("firstName") ?: ""
                        val lastName = userDoc.getString("lastName") ?: ""

                        //create user and profile model objects
                        val userObj = Model.User(savedEmail, "")
                        val profileObj = Model.Profile(userId, firstName, lastName) //removable likely once SessionMgr untangled

                        //store session
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
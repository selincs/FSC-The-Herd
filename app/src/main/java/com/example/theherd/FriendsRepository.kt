package com.example.theherd

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRepository {

    //Send Friend request - email
    //user types in email, check email in path
//    users.whereEqualTo("email", inputEmail)
//write request to      users/{targetID}/friendRequests/{currentUserID}

    //Get users target email, filter out @Farmingdale case
    //TODO: Validate for if document already exists : requestRef.get()
    // & dont send if already friends : users/{currentUserID}/friends/{targetUserID}
    fun sendFriendRequest(
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        //The ID of the user making the request
        val currentUserID = SessionManager.requireUserId()

        //The email the Friend Request is sent to-- Fnc normalizes email to handle "email" or "email@farmingdale.edu"
        val email = normalizeEmail(username)

        //TODO Validations here later

        // Find target user by email
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    println("User not found")
                    onFailure(Exception("User not found"))
                    return@addOnSuccessListener
                }

                val targetDoc = result.documents[0]
                val targetUserID = targetDoc.id

                if (targetUserID == currentUserID) {
                    println("You cannot add yourself")
                    onFailure(Exception("You cannot add yourself"))
                    return@addOnSuccessListener
                }

                // Get sender email
                getCurrentUserEmail(
                    onSuccess = { senderEmail ->

                        val requestRef = db.collection("users")
                            .document(targetUserID)
                            .collection("friendRequests")
                            .document(currentUserID)

                        val requestData = hashMapOf(
                            "fromUserID" to currentUserID,
                            "fromEmail" to senderEmail,
                            "status" to "pending",
                            "sentAt" to FieldValue.serverTimestamp()
                        )

                        requestRef.set(requestData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    },
                    onFailure = { onFailure(it) }
                )
            }
            .addOnFailureListener { onFailure(it) }
    }

    //Accounts for email / username entries without the @farmingdale.edu in sending Friend Requests
    //User types "email", returns "email@farmingdale.edu" to find in Firestore
    fun normalizeEmail(input: String): String {
        val trimmed = input.trim().lowercase()

        //Return email if the user input has a "@" symbol - guaranteed in our sign up GUI
        return if (trimmed.contains("@")) {
            trimmed
        } else {    //Else append to trimmed
            "$trimmed@farmingdale.edu"
        }
    }

    //Returns the email of the current user making the friend request
    fun getCurrentUserEmail(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userID = SessionManager.requireUserId()

        db.collection("users")
            .document(userID)
            .get()
            .addOnSuccessListener { doc ->
                val email = doc.getString("email")
                if (email != null) {
                    onSuccess(email)
                } else {
                    onFailure(Exception("Email not found in FriendsRepo getCurrUserEmail"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

//    fun loadFriends(
//        onSuccess: (List<Friend>) -> Unit,
//        onFailure: (Exception) -> Unit
//    ) {
//        val db = FirebaseFirestore.getInstance()
//        val userID = SessionManager.requireUserId()
//
//        db.collection("users")
//            .document(userID)
//            .collection("friends")
//            .get()
//            .addOnSuccessListener { docs ->
//
//                val friends = docs.map { doc ->
//                    Friend(
//                        friendID = doc.id,
//                        displayName = doc.getString("displayName") ?: "",
//                        latestStatus = doc.getString("latestStatusPost") ?: "",
//                        onlineStatus = doc.getString("onlineStatus") ?: "offline"
//                    )
//                }
//
//                onSuccess(friends)
//            }
//            .addOnFailureListener { onFailure(it) }
//    }
}
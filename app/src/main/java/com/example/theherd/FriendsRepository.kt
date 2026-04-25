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
    //TODO: If 2 users request each other, if one FriendReq is accepted, make sure to clean up the other list
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

    //Get user's incoming friend requests from Firestore for display in the Requests Tab
//    println("User has no current friend requests, do something here")
    fun getIncomingFriendRequests(
        onSuccess: (List<Friend>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()

        db.collection("users")
            .document(currentUserID)
            .collection("friendRequests")
            .get()
            .addOnSuccessListener { requestDocs ->

                if (requestDocs.isEmpty) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val requests = mutableListOf<Friend>()
                var remaining = requestDocs.size()  //remaining friend requests

                //For each document in the friend request sub collection, create a friend entry until none remain
                for (doc in requestDocs) {
                    val fromUserID = doc.getString("fromUserID") ?: continue

                    //enter users collection -> in userDoc(foundByID) -> get fields
                    db.collection("users")
                        .document(fromUserID)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""
                            val fullName = "$firstName $lastName".trim()    //combine fName+lName for display name

                            val isOnline = userDoc.getString("onlineStatus") == "online" //only online atm?

                            //create Friend entry for display, id, name, statusText, online status, isFriend=false(for friend request)
                            val friend = Friend(
                                id = fromUserID,
                                name = fullName,
                                statusText = "Sent you a friend request", // replaces "3 mutual friends" in hardcode
                                isOnline = isOnline,
                                isFriend = false
                            )

                            requests.add(friend)

                            remaining--
                            if (remaining == 0) {
                                onSuccess(requests)
                            }
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) {
                                onSuccess(requests)
                            }
                        }
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
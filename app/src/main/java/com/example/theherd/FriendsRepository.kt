package com.example.theherd

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FriendsRepository {

    fun loadFriends(
        onSuccess: (List<Friend>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()

        db.collection("users")
            .document(currentUserID)
            .collection("friends")
            .get()
            .addOnSuccessListener { friendDocs ->

                if (friendDocs.isEmpty) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val friendsList = mutableListOf<Friend>()
                var remaining = friendDocs.size()

                for (doc in friendDocs) {
                    val friendID = doc.getString("userID") ?: continue

                    db.collection("users")
                        .document(friendID)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""
                            val fullName = "$firstName $lastName".trim()

                            val isOnline = userDoc.getString("onlineStatus") == "online"

                            val friend = Friend(
                                id = friendID,
                                name = fullName,
                                statusText = "No recent status", // placeholder for now
                                isOnline = isOnline,
                                isFriend = true
                            )

                            friendsList.add(friend)

                            remaining--
                            if (remaining == 0) {
                                onSuccess(friendsList)
                            }
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) {
                                onSuccess(friendsList)
                            }
                        }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    //Removes a friend from both the user's friends list + the friend list of the removed friend in Firestore
    fun removeFriend(
        friendUserID: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()

        val currentUserRef = db.collection("users").document(currentUserID)
        val otherUserRef = db.collection("users").document(friendUserID)

        val batch = db.batch()

        // Remove friend from current user's list
        val myFriendRef = currentUserRef
            .collection("friends")
            .document(friendUserID)

        batch.delete(myFriendRef)

        // Remove current user from other user's list
        val theirFriendRef = otherUserRef
            .collection("friends")
            .document(currentUserID)

        batch.delete(theirFriendRef)

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    //Send Friend request - email
    //user types in email, check email in path
//    users.whereEqualTo("email", inputEmail)
//write request to      users/{targetID}/friendRequests/{currentUserID}

    //Get users target email, filter out @Farmingdale case
    //Send a friend request to another user, with auto accepting mutual requests
    fun sendFriendRequest(
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()
        val email = normalizeEmail(username)

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    println("User not found for friend request sending")
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

                val currentUserRef = db.collection("users").document(currentUserID)
                val targetUserRef = db.collection("users").document(targetUserID)

                // 🔴 1. Check if CURRENT USER has blocked TARGET
                currentUserRef.collection("blockedUsers")
                    .document(targetUserID)
                    .get()
                    .addOnSuccessListener { blockedDoc ->

                        if (blockedDoc.exists()) {
                            println("You have blocked this user")
                            onFailure(Exception("You have blocked this user"))
                            return@addOnSuccessListener
                        }

                        // 🔴 2. Check if TARGET has blocked CURRENT USER
                        targetUserRef.collection("blockedUsers")
                            .document(currentUserID)
                            .get()
                            .addOnSuccessListener { blockedByTargetDoc ->

                                if (blockedByTargetDoc.exists()) {
                                    println("Friend request attempt sent to blocked user")
                                    onFailure(Exception("You cannot send a request to this user"))
                                    return@addOnSuccessListener
                                }

                                // 🟢 3. Check if already friends
                                currentUserRef.collection("friends")
                                    .document(targetUserID)
                                    .get()
                                    .addOnSuccessListener { friendDoc ->

                                        if (friendDoc.exists()) {
                                            println("You are already friends")
                                            onFailure(Exception("You are already friends"))
                                            return@addOnSuccessListener
                                        }

                                        // 🟢 4. Check if request already sent
                                        targetUserRef.collection("friendRequests")
                                            .document(currentUserID)
                                            .get()
                                            .addOnSuccessListener { existingRequest ->

                                                if (existingRequest.exists()) {
                                                    println("Friend request already sent")
                                                    onFailure(Exception("Friend request already sent"))
                                                    return@addOnSuccessListener
                                                }

                                                // 🟢 5. Check for mutual request
                                                currentUserRef.collection("friendRequests")
                                                    .document(targetUserID)
                                                    .get()
                                                    .addOnSuccessListener { reverseRequest ->

                                                        if (reverseRequest.exists()) {
                                                            // 🔥 AUTO ACCEPT
                                                            acceptFriendRequest(targetUserID) { success ->
                                                                if (success) {
                                                                    onSuccess()
                                                                } else {
                                                                    println("Failed to auto-accept request")
                                                                    onFailure(Exception("Failed to auto-accept request"))
                                                                }
                                                            }
                                                            return@addOnSuccessListener
                                                        }

                                                        // 🟢 6. Safe to create request
                                                        println("Creating friend request")
                                                        createRequest(
                                                            targetUserID,
                                                            currentUserID,
                                                            onSuccess,
                                                            onFailure
                                                        )
                                                    }
                                                    .addOnFailureListener { onFailure(it) }
                                            }
                                            .addOnFailureListener { onFailure(it) }
                                    }
                                    .addOnFailureListener { onFailure(it) }
                            }
                            .addOnFailureListener { onFailure(it) }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }


    //Helper for sendFriendRequest, creates the actual request after the validation logic for users sending friend requests
    private fun createRequest(
        targetUserID: String,
        currentUserID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

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

    //Accept the friend request, removes the request, NEEDS TO HANDLE MUTUAL REQUESTS STILL
    //Adds each user to each others friends collection, removes the request, (mutual requests incomplete)
    fun acceptFriendRequest(
        fromUserID: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()

        val currentUserRef = db.collection("users").document(currentUserID)
        val otherUserRef = db.collection("users").document(fromUserID)

        val batch = db.batch()

        // Add friend to current user
        val myFriendRef = currentUserRef
            .collection("friends")
            .document(fromUserID)

        batch.set(myFriendRef, mapOf(
            "userID" to fromUserID,
            "addedAt" to FieldValue.serverTimestamp()
        ))

        // Add current user to other user's friends
        val theirFriendRef = otherUserRef
            .collection("friends")
            .document(currentUserID)

        batch.set(theirFriendRef, mapOf(
            "userID" to currentUserID,
            "addedAt" to FieldValue.serverTimestamp()
        ))

        //Case 1: Remove friend request (Normal case, user A sent request to user B with no pending requests)
        val requestRef = currentUserRef
            .collection("friendRequests")
            .document(fromUserID)

        batch.delete(requestRef)

        //Case 2: If both users friend requested each other, remove the request from both accounts
        val reverseRequestRef = otherUserRef
            .collection("friendRequests")
            .document(currentUserID)

        batch.delete(reverseRequestRef)

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun rejectFriendRequest(
        fromUserID: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = SessionManager.requireUserId()

        db.collection("users")
            .document(currentUserID)
            .collection("friendRequests")
            .document(fromUserID)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun blockUser(
        targetUserId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        val batch = db.batch()

        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(targetUserId)

        // 1. Add to blocked list
        val blockRef = currentUserRef
            .collection("blockedUsers")
            .document(targetUserId)

        batch.set(blockRef, hashMapOf(
            "userID" to targetUserId,
            "blockedAt" to FieldValue.serverTimestamp()
        ))

        // 2. Remove from friends (both sides)
        val currentFriendRef = currentUserRef
            .collection("friends")
            .document(targetUserId)

        val targetFriendRef = targetUserRef
            .collection("friends")
            .document(currentUserId)

        batch.delete(currentFriendRef)
        batch.delete(targetFriendRef)

        // 3. Delete friend requests (both directions)

        val incomingRequest = currentUserRef
            .collection("friendRequests")
            .document(targetUserId)

        val outgoingRequest = targetUserRef
            .collection("friendRequests")
            .document(currentUserId)

        batch.delete(incomingRequest)
        batch.delete(outgoingRequest)

        // Commit everything atomically
        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    //Unvalidated unblockUser() fnc for Firestore TODO: Validate fnc
    fun unblockUser(
        targetUserId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        val blockRef = db.collection("users")
            .document(currentUserId)
            .collection("blockedUsers")
            .document(targetUserId)

        blockRef.delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    //Unvalidated getBlockedUsers() fnc for Firestore TODO: Validate fnc
    fun getBlockedUsers(
        onSuccess: (List<Friend>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        db.collection("users")
            .document(currentUserId)
            .collection("blockedUsers")
            .get()
            .addOnSuccessListener { docs ->

                if (docs.isEmpty) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val blockedIds = docs.map { it.id }

                val blockedUsers = mutableListOf<Friend>()

                // Fetch user data for each blocked user
                var remaining = blockedIds.size

                for (userId in blockedIds) {
                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""

                            blockedUsers.add(
                                Friend(
                                    id = userId,
                                    name = "$firstName $lastName".trim(),
                                    isOnline = false, // optional
                                    statusText = "Blocked",
                                    isFriend = false
                                )
                            )

                            remaining--
                            if (remaining == 0) {
                                onSuccess(blockedUsers)
                            }
                        }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }



}
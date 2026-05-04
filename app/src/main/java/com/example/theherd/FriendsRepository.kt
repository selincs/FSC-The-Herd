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
                                statusText = "No recent status", // TODO: placeholder for now, add Firestore field
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
    //write request to users/{targetID}/friendRequests/{currentUserID}
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
                    println("User not found for sendFriendRequest() to $email - FriendsRepo")
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
                                                            email,  //pass the receiver email to helper
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
        targetEmail: String, // passed from sendFriendRequest
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        getCurrentUserEmail(
            onSuccess = { senderEmail ->

                val batch = db.batch()

                // 1. Incoming request (receiver side)
                val incomingRef = db.collection("users")
                    .document(targetUserID)
                    .collection("friendRequests")
                    .document(currentUserID)

                val incomingData = hashMapOf(
                    "fromUserID" to currentUserID,
                    "fromEmail" to senderEmail,
                    "type" to "incoming",
                    "status" to "pending friend request",
                    "sentAt" to FieldValue.serverTimestamp()
                )

                batch.set(incomingRef, incomingData)

                // 2. Outgoing request (sender side)
                val outgoingRef = db.collection("users")
                    .document(currentUserID)
                    .collection("sentFriendRequests")
                    .document(targetUserID)

                val outgoingData = hashMapOf(
                    "toUserID" to targetUserID,
                    "toEmail" to targetEmail, // Passed after being normalized in sendFriendRequest()
                    "type" to "outgoing",
                    "status" to "pending friend request",
                    "sentAt" to FieldValue.serverTimestamp()
                )

                batch.set(outgoingRef, outgoingData)

                // 3. Commit
                batch.commit()
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

    fun getAllRequests(onResult: (List<Friend>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        val requests = mutableListOf<Friend>()
        val currentUserRef = db.collection("users").document(currentUserId)

        // Track async Firestore operations
        var totalOperations = 0
        var completedOperations = 0

        fun checkDone() {
            if (completedOperations == totalOperations) {
                onResult(requests)
            }
        }

        // 1. Incoming Requests
        currentUserRef.collection("friendRequests")
            .get()
            .addOnSuccessListener { incomingDocs ->

                totalOperations += incomingDocs.size()

                if (incomingDocs.isEmpty) {
                    checkDone()
                }

                for (doc in incomingDocs) {
                    val fromUserID = doc.getString("fromUserID") ?: continue

                    db.collection("users")
                        .document(fromUserID)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val firstName = userDoc.getString("firstName") ?: ""
                            val lastName = userDoc.getString("lastName") ?: ""
                            val fullName = "$firstName $lastName".trim()
                            val isOnline = userDoc.getString("onlineStatus") == "online"

                            requests.add(
                                Friend(
                                    id = fromUserID,
                                    name = fullName,
                                    statusText = "Sent you a friend request",
                                    isOnline = isOnline,
                                    isFriend = false,
                                    isIncoming = true
                                )
                            )

                            completedOperations++
                            checkDone()
                        }
                        .addOnFailureListener {
                            completedOperations++
                            checkDone()
                        }
                }

                // 2. Outgoing Requests
                currentUserRef.collection("sentFriendRequests")
                    .get()
                    .addOnSuccessListener { outgoingDocs ->

                        totalOperations += outgoingDocs.size()

                        if (outgoingDocs.isEmpty) {
                            checkDone()
                        }

                        for (doc in outgoingDocs) {
                            val toUserID = doc.getString("toUserID") ?: continue

                            db.collection("users")
                                .document(toUserID)
                                .get()
                                .addOnSuccessListener { userDoc ->

                                    val firstName = userDoc.getString("firstName") ?: ""
                                    val lastName = userDoc.getString("lastName") ?: ""
                                    val fullName = "$firstName $lastName".trim()
                                    val isOnline = userDoc.getString("onlineStatus") == "online"

                                    requests.add(
                                        Friend(
                                            id = toUserID,
                                            name = fullName,
                                            statusText = "Friend request sent! Waiting on their reply.",
                                            isOnline = isOnline,
                                            isFriend = false,
                                            isIncoming = false
                                        )
                                    )

                                    completedOperations++
                                    checkDone()
                                }
                                .addOnFailureListener {
                                    completedOperations++
                                    checkDone()
                                }
                        }
                    }
                    .addOnFailureListener {
                        onResult(requests)
                    }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun cancelFriendRequest(
        targetUserId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        val batch = db.batch()

        val currentUserRef = db.collection("users").document(currentUserId)
        val targetUserRef = db.collection("users").document(targetUserId)

        // Remove outgoing request (sender side)
        val sentRef = currentUserRef
            .collection("sentFriendRequests")
            .document(targetUserId)

        // Remove incoming request (receiver side)
        val incomingRef = targetUserRef
            .collection("friendRequests")
            .document(currentUserId)

        batch.delete(sentRef)
        batch.delete(incomingRef)

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
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


    //TODO: Move below functions to BlockRepository if implemented
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

    fun searchGlobalUsers(
        query: String,
        filterType: String,
        onResult: (List<Friend>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = SessionManager.requireUserId()

        // dropdown fields will be matched to FS document field
        val field = when (filterType) {
            "First Name" -> "firstName"
            "Last Name" -> "lastName"
            "Email" -> "email"
            else -> "firstName"
        }

        val normalizedQuery = query.lowercase().trim()

        db.collection("users")
            .whereEqualTo(field, normalizedQuery)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = querySnapshot.documents.mapNotNull { doc ->
                    if (doc.id == currentUserId) return@mapNotNull null // Skip yourself

                    Friend(
                        id = doc.id,
                        name = "${doc.getString("firstName")} ${doc.getString("lastName")}".trim(),
                        statusText = doc.getString("major") ?: "Student",
                        isOnline = doc.getString("onlineStatus") == "online",
                        isFriend = false
                    )
                }
                onResult(userList)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
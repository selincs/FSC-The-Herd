package com.example.theherd

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object BlockListRepository {

    private val db = FirebaseFirestore.getInstance()


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

    fun unblockUser(
        targetUserId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val currentUserId = SessionManager.requireUserId()

        db.collection("users")
            .document(currentUserId)
            .collection("blockedUsers")
            .document(targetUserId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getBlockedUsers(
        onSuccess: (List<Friend>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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

                val blockedUsers = mutableListOf<Friend>()
                var remaining = docs.size()

                for (doc in docs) {
                    val userId = doc.id

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
                                    statusText = "Blocked",
                                    isOnline = false,
                                    isFriend = false
                                )
                            )

                            remaining--
                            if (remaining == 0) {
                                onSuccess(blockedUsers)
                            }
                        }
                        .addOnFailureListener {
                            // still decrement so we don't hang forever
                            remaining--
                            if (remaining == 0) {
                                onSuccess(blockedUsers)
                            }
                        }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
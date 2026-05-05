package com.example.theherd

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object MessageRepository {
    fun getConversationId(user1: String, user2: String): String {
        return listOf(user1, user2).sorted().joinToString("_")
    }

    fun sendMessage(convoId: String, message: Message) {
        println("Sending message in repo sendmsg()")
        val db = FirebaseFirestore.getInstance()

        val convoRef = db.collection("conversations").document(convoId)

        val messageData = hashMapOf(
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "text" to message.text,
            "timestamp" to FieldValue.serverTimestamp() // 🔥 FIX
        )
        // 1. Write message
        convoRef.collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                println("Message sent successfully")
                // 2. Update shared conversation metadata
                val convoMeta = mapOf(
                    "lastMessage" to message.text,
                    "lastTimestamp" to FieldValue.serverTimestamp(),
                    "participants" to listOf(message.senderId, message.receiverId)
                )

                convoRef.set(convoMeta, com.google.firebase.firestore.SetOptions.merge())
                // 3. Update BOTH users' conversation lists
                val senderMeta = mapOf(
                    "lastMessage" to message.text,
                    "lastTimestamp" to FieldValue.serverTimestamp(),
                    "otherUserId" to message.receiverId
                )

                val receiverMeta = mapOf(
                    "lastMessage" to message.text,
                    "lastTimestamp" to FieldValue.serverTimestamp(),
                    "otherUserId" to message.senderId
                )

                db.collection("users")
                    .document(message.senderId)
                    .collection("conversations")
                    .document(convoId)
                    .set(senderMeta)

                db.collection("users")
                    .document(message.receiverId)
                    .collection("conversations")
                    .document(convoId)
                    .set(receiverMeta)
            }
            .addOnFailureListener {
                println("FAILED to send message: ${it.message}")
            }
        println("msg sent")
    }

    fun listenForMessages(
        convoId: String,
        onUpdate: (List<Message>) -> Unit
    ) {
        println("listening 4 msgs")
        val db = FirebaseFirestore.getInstance()

        db.collection("conversations")
            .document(convoId)
            .collection("messages")
            .orderBy("timestamp") // primary
            .addSnapshotListener { snapshot, _ ->

                val messages = snapshot?.documents?.mapNotNull {
                    try {
                        val msg = it.toObject(Message::class.java)
                        msg?.copy(id = it.id)
                    } catch (e: Exception) {
                        println("🔥 PARSE ERROR for doc ${it.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()

                onUpdate(messages)
            }
        println("msgs listened 4")
    }

}
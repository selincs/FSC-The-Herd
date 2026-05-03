package com.example.theherd

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object GuideFAQRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getQuestions(
        categoryName: String,
        onResult: (List<Map<String, Any>>) -> Unit
    ) {
        db.collection("guideArchive")
            .document(categoryName)
            .collection("questions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val questions = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("questionID", doc.id)
                    }
                }

                onResult(questions)
            }
    }

    fun addQuestion(
        categoryName: String,
        questionText: String,
        onDone: (Boolean) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onDone(false)
            return
        }

        val questionData = hashMapOf(
            "questionText" to questionText,
            "askedByUID" to user.uid,
            "username" to (user.email?.substringBefore("@") ?: "Student"),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("guideArchive")
            .document(categoryName)
            .collection("questions")
            .add(questionData)
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                onDone(false)
            }
    }
}
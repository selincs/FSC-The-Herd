package com.example.theherd

import Model.Guide
import Model.GuideAnswer
import Model.GuideQuestion
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


object GuideRepository {

    private val db = FirebaseFirestore.getInstance()


    private val allGuides = listOf(
        Guide(
            "101",
            "Finding the Hidden Science Lab Classrooms",
            "A quick walkthrough from the main quad.",
            true,
            false,
            "Navigation"
        ),
        Guide(
            "102",
            "FSC Shuttle Bus Schedule",
            "Times and stops for the campus loop.",
            false,
            true,
            "Travel"
        ),
        Guide(
            "201",
            "How to Register for Classes",
            "Step-by-step for the new system.",
            true,
            false,
            "Academic"
        ),
        Guide(
            "202",
            "FAFSA Deadline Guide",
            "Important dates for this semester.",
            true,
            false,
            "Financial Aid"
        ),
        Guide(
            "301",
            "Dorm Room Essentials",
            "What you can and can't bring.",
            false,
            true,
            "Housing"
        ),
        Guide(
            "302",
            "Joining 'The Herd' Club",
            "How to get involved on campus.",
            true,
            false,
            "Clubs"
        )
    )

    fun getAllGuides(): List<Guide> {
        return allGuides
    }

    fun getGuideById(id: String?): Guide? {
        return allGuides.find { it.id == id }
    }

    fun createGuide(
        title: String,
        category: String,
        description: String,
        onDone: (Boolean) -> Unit
    ) {
        val guideRef = FirestoreDatabase.guides.document()

        val guideData = hashMapOf(
            "id" to guideRef.id,
            "title" to title,
            "description" to description,
            "category" to category,
            "verified" to false,
            "userSuggested" to true,
            "helpfulCount" to 0,
            "notHelpfulCount" to 0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        guideRef.set(guideData)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun addQuestion(
        guideId: String,
        questionText: String,
        username: String,
        onDone: (Boolean) -> Unit
    ) {
        val questionRef = db.collection("guides")
            .document(guideId)
            .collection("questions")
            .document()

        val questionData = hashMapOf(
            "questionId" to questionRef.id,
            "questionText" to questionText,
            "username" to username,
            "timestamp" to System.currentTimeMillis(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        questionRef.set(questionData)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun getGuidesFromFirestore(
        onDone: (List<Guide>) -> Unit
    ) {
        FirestoreDatabase.guides
            .get()
            .addOnSuccessListener { result ->
                val guides = result.documents.mapNotNull { doc ->
                    doc.toObject(Guide::class.java)
                }

                onDone(guides)
            }
            .addOnFailureListener {
                onDone(emptyList())
            }
    }

    fun voteGuide(
        guideId: String,
        newVote: String,
        onDone: (Boolean) -> Unit
    ) {

        val userId = FirestoreAuthManager.currentUserId ?: run {
            onDone(false)
            return
        }

        val voteRef = FirestoreDatabase.guides
            .document(guideId)
            .collection("votes")
            .document(userId)

        val guideRef = FirestoreDatabase.guides
            .document(guideId)

        voteRef.get()
            .addOnSuccessListener { doc ->

                val oldVote = doc.getString("vote")

                var helpfulChange = 0
                var notHelpfulChange = 0
                var finalVote = newVote

                if (oldVote == newVote) {

                    finalVote = ""

                    if (newVote == "up") {
                        helpfulChange = -1
                    } else {
                        notHelpfulChange = -1
                    }

                } else {

                    if (oldVote == "up") {
                        helpfulChange -= 1
                    }

                    if (oldVote == "down") {
                        notHelpfulChange -= 1
                    }

                    if (newVote == "up") {
                        helpfulChange += 1
                    }

                    if (newVote == "down") {
                        notHelpfulChange += 1
                    }
                }

                val batch = FirestoreDatabase.db.batch()

                batch.update(
                    guideRef,
                    mapOf(
                        "helpfulCount" to FieldValue.increment(helpfulChange.toLong()),
                        "notHelpfulCount" to FieldValue.increment(notHelpfulChange.toLong())
                    )
                )

                if (finalVote.isBlank()) {
                    batch.delete(voteRef)
                } else {
                    batch.set(voteRef, mapOf("vote" to finalVote))
                }

                batch.commit()
                    .addOnSuccessListener {
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

    fun getGuideFromFirestoreById(
        guideId: String,
        onDone: (Guide?) -> Unit
    ) {
        FirestoreDatabase.guides
            .document(guideId)
            .get()
            .addOnSuccessListener { doc ->
                val guide = doc.toObject(Guide::class.java)
                onDone(guide)
            }
            .addOnFailureListener {
                onDone(null)
            }
    }

    fun getQuestions(
        guideId: String,
        onDone: (List<GuideQuestion>) -> Unit
    ) {
        db.collection("guides")
            .document(guideId)
            .collection("questions")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val questions = result.documents.mapNotNull { doc ->
                    GuideQuestion(
                        doc.getString("questionId") ?: doc.id,
                        doc.getString("questionText") ?: "",
                        doc.getString("username") ?: "Anonymous",
                        doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        doc.getString("topAnswer")
                    )
                }

                onDone(questions)
            }
            .addOnFailureListener {
                onDone(emptyList())
            }
    }

    fun addAnswer(
        guideId: String,
        questionId: String,
        answerText: String,
        username: String,
        onDone: (Boolean) -> Unit
    ) {
        val answerRef = db.collection("guides")
            .document(guideId)
            .collection("questions")
            .document(questionId)
            .collection("answers")
            .document()

        val answerData = hashMapOf(
            "answerId" to answerRef.id,
            "answerText" to answerText,
            "username" to username,
            "timestamp" to System.currentTimeMillis(),
            "createdAt" to FieldValue.serverTimestamp(),
            "upvotes" to 0,
            "downvotes" to 0,
            "currentUserVote" to ""
        )

        answerRef.set(answerData)
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                onDone(false)
            }
    }

    fun getAnswers(
        guideId: String,
        questionId: String,
        onDone: (List<GuideAnswer>) -> Unit
    ) {
        db.collection("guides")
            .document(guideId)
            .collection("questions")
            .document(questionId)
            .collection("answers")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val answers = result.documents.mapNotNull { doc ->
                    GuideAnswer(
                        doc.getString("answerId") ?: doc.id,
                        doc.getString("answerText") ?: "",
                        doc.getString("username") ?: "Anonymous",
                        doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        doc.getLong("upvotes")?.toInt() ?: 0,
                        doc.getLong("downvotes")?.toInt() ?: 0,
                        doc.getString("currentUserVote") ?: ""
                    )
                }

                onDone(answers)
            }
            .addOnFailureListener {
                onDone(emptyList())
            }
    }

    fun voteAnswer(
        guideId: String,
        questionId: String,
        answer: GuideAnswer,
        newVote: String,
        onDone: (Boolean) -> Unit
    ) {
        val answerRef = db.collection("guides")
            .document(guideId)
            .collection("questions")
            .document(questionId)
            .collection("answers")
            .document(answer.answerId)

        val oldVote = answer.currentUserVote

        var upChange = 0
        var downChange = 0
        var finalVote = newVote

        if (oldVote == newVote) {
            finalVote = ""

            if (newVote == "up") {
                upChange = -1
            } else {
                downChange = -1
            }
        } else {
            if (oldVote == "up") {
                upChange -= 1
            }

            if (oldVote == "down") {
                downChange -= 1
            }

            if (newVote == "up") {
                upChange += 1
            }

            if (newVote == "down") {
                downChange += 1
            }
        }

        answerRef.update(
            mapOf(
                "upvotes" to FieldValue.increment(upChange.toLong()),
                "downvotes" to FieldValue.increment(downChange.toLong()),
                "currentUserVote" to finalVote
            )
        )
            .addOnSuccessListener {
                updateTopAnswer(guideId, questionId) {
                    onDone(true)
                }
            }
            .addOnFailureListener { onDone(false) }
    }
    private fun updateTopAnswer(
        guideId: String,
        questionId: String,
        onDone: () -> Unit
    ) {
        val questionRef = db.collection("guides")
            .document(guideId)
            .collection("questions")
            .document(questionId)

        questionRef.collection("answers")
            .get()
            .addOnSuccessListener { result ->

                val topDoc = result.documents
                    .filter { doc ->
                        val upvotes = doc.getLong("upvotes") ?: 0
                        upvotes > 0
                    }
                    .maxByOrNull { doc ->
                        val upvotes = doc.getLong("upvotes") ?: 0
                        val downvotes = doc.getLong("downvotes") ?: 0
                        upvotes - downvotes
                    }

                val topAnswerText = topDoc?.getString("answerText") ?: ""

                questionRef.update("topAnswer", topAnswerText)
                    .addOnCompleteListener {
                        onDone()
                    }
            }
            .addOnFailureListener {
                onDone()
            }
    }
}
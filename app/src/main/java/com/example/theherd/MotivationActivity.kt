package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog


class MotivationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation) // connects XML
        setupNavigation() // sets up all buttons in the tool/nav bar

        val mentorsRecyclerView = findViewById<RecyclerView>(R.id.mentorsRecyclerView)
        val commitmentsRecyclerView = findViewById<RecyclerView>(R.id.commitmentsRecyclerView)

        mentorsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        commitmentsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //Fake Data -- Can remove when firebase is connected
        val fakeMentors = listOf(Mentor("Rachel Green", "Mentor"), Mentor("Ross Geller", "Mentor"), Mentor("Monica Geller", "Mentor"), Mentor("Chandler Bing", "Mentor"), Mentor("Joey Tribbiani", "Mentor"), Mentor("Phoebe Buffay", "Mentor"))
        val fakeCommitments = listOf(Commitment("Go to Gym", "Chandler", 3), Commitment("Study", "Monica", 5), Commitment("Go for a hike", "Joey", 9), Commitment("Self Defense", "Rachel", 15))

        mentorsRecyclerView.adapter = MentorAdapter(fakeMentors)

        commitmentsRecyclerView.adapter = CommitmentAdapter(fakeCommitments) { clickedCommitment ->
            val bottomSheet = BottomSheetDialog(this)
            bottomSheet.setContentView(R.layout.bottom_sheet_commitment)

            val titleText = bottomSheet.findViewById<TextView>(R.id.detailActivityName)
            val partnerText = bottomSheet.findViewById<TextView>(R.id.detailPartnerName)
            val streakNum = bottomSheet.findViewById<TextView>(R.id.detailStreakNumber)

            titleText?.text = clickedCommitment.activityName
            partnerText?.text = "Shared commitment with ${clickedCommitment.partnerName}"
            streakNum?.text = "🔥 ${clickedCommitment.streak} Days"

            bottomSheet.show()
        }

    }
}
// data class Mentor(val name: String, val role: String)
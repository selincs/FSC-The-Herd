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
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.card.MaterialCardView

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
        val fakeMentors = mutableListOf(
            Mentor("Rachel Green", "Mentor"),
            Mentor("Ross Geller", "Mentor"),
            Mentor("Monica Geller", "Mentor"),
            Mentor("Chandler Bing", "Mentor"),
            Mentor("Joey Tribbiani", "Mentor"),
            Mentor("Phoebe Buffay", "Mentor")
        )

        val fakeCommitments = mutableListOf(
            Commitment("Go to Gym", "Chandler", 3, 1),
            Commitment("Study", "Monica", 5, 1),
            Commitment("Go for a hike", "Joey", 9, 1),
            Commitment("Self Defense", "Rachel", 8, 1)
        )

        mentorsRecyclerView.adapter = MentorAdapter(fakeMentors)

        commitmentsRecyclerView.adapter = CommitmentAdapter(fakeCommitments) { clickedCommitment ->
            val bottomSheet = BottomSheetDialog(this)
            bottomSheet.setContentView(R.layout.bottom_sheet_commitment)

            val titleText = bottomSheet.findViewById<TextView>(R.id.detailActivityName)
            val partnerText = bottomSheet.findViewById<TextView>(R.id.detailPartnerName)
            val streakNum = bottomSheet.findViewById<TextView>(R.id.detailStreakNumber)
            val levelText = bottomSheet.findViewById<TextView>(R.id.detailLevelText)
            val completeBtn = bottomSheet.findViewById<Button>(R.id.btnMarkComplete)

            titleText?.text = clickedCommitment.activityName
            if (clickedCommitment.partnerName.isEmpty()) {
                partnerText?.text = "Going solo"
            } else {
                partnerText?.text = "Shared commitment with ${clickedCommitment.partnerName}"
            }
            streakNum?.text = "🔥 ${clickedCommitment.streak} Days"
            levelText?.text = "Level ${clickedCommitment.level}"

            completeBtn?.setOnClickListener {

                clickedCommitment.streak += 1

                if (clickedCommitment.streak >= 10) {
                    clickedCommitment.streak = 0
                    clickedCommitment.level += 1
                }

                streakNum?.text = "🔥 ${clickedCommitment.streak} Days"
                levelText?.text = "Level ${clickedCommitment.level}"

                commitmentsRecyclerView.adapter?.notifyDataSetChanged()
                bottomSheet.dismiss() // Should clicking the complete button dismiss the bottom sheet?
            }



            bottomSheet.show()
        }

        val becomeMentorCard = findViewById<MaterialCardView>(R.id.cardBecomeMentor)

        becomeMentorCard.setOnClickListener {
            val mentorSignupSheet = BottomSheetDialog(this)
            mentorSignupSheet.setContentView(R.layout.bottom_sheet_mentor_signup)

            val inputName = mentorSignupSheet.findViewById<EditText>(R.id.inputMentorName)
            val inputRole = mentorSignupSheet.findViewById<EditText>(R.id.inputMentorRole)
            val btnSubmit = mentorSignupSheet.findViewById<Button>(R.id.btnSubmitMentor)

            btnSubmit?.setOnClickListener {
                val nameText = inputName?.text.toString()
                val roleText = inputRole?.text.toString()

                if (nameText.isNotEmpty() && roleText.isNotEmpty()) {

                    val newMentor = Mentor(nameText, roleText)

                    fakeMentors.add(0, newMentor)

                    mentorsRecyclerView.adapter?.notifyItemInserted(0)
                    mentorsRecyclerView.scrollToPosition(0)

                    Toast.makeText(this, "Thank you for becoming a mentor!", Toast.LENGTH_SHORT).show()
                    mentorSignupSheet.dismiss()
                } else {
                    Toast.makeText(this, "Please fill out both fields!", Toast.LENGTH_SHORT).show()
                }
            }

            mentorSignupSheet.show()

        }

        val fabAddCommitment = findViewById<FloatingActionButton>(R.id.fabAddCommitment)

        fabAddCommitment.setOnClickListener {
            val newGoalSheet = BottomSheetDialog(this)
            newGoalSheet.setContentView(R.layout.bottom_sheet_new_commitment)

            val inputActivity = newGoalSheet.findViewById<EditText>(R.id.inputActivityName)
            val inputPartner = newGoalSheet.findViewById<EditText>(R.id.inputPartnerName)
            val btnSave = newGoalSheet.findViewById<Button>(R.id.btnSaveCommitment)

            btnSave?.setOnClickListener {

                val activityText = inputActivity?.text.toString()
                val partnerText = inputPartner?.text.toString().trim()


                if (activityText.isNotEmpty()) {
                    val newGoal = Commitment(activityText, partnerText, 0, 1)
                    fakeCommitments.add(newGoal)

                    commitmentsRecyclerView.adapter?.notifyDataSetChanged()
                    commitmentsRecyclerView.scrollToPosition(fakeCommitments.size - 1)
                    newGoalSheet.dismiss()
                }else {
                    Toast.makeText(this, "Please fill out both fields!", Toast.LENGTH_SHORT).show()
                }
            }

            newGoalSheet.show()
        }



    }
}
// data class Mentor(val name: String, val role: String)